/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package documentclustering;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.Vector;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.CosineSimilarity;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 *
 * @author Lorenzo Fundaró <lorenzofundaro [at] yahoo.com>
 */
public class Documentclustering {

    public static boolean stillDirty(String term){
        if (term.matches("[“”\'.,;:@\"!¿¡/?()]+"))
            return true;
        return false;
    }

    public static String clean(String dirty){

        dirty = dirty.replaceAll("^[“”.,;\':\"!¿¡?/()]+", "");
        dirty = dirty.replaceAll("[“”.,;\':\"!¿¡?/()]+$", "");

        dirty = dirty.replaceAll("á", "a");
        dirty = dirty.replaceAll("é", "e");
        dirty = dirty.replaceAll("í", "i");
        dirty = dirty.replaceAll("ó", "o");
        dirty = dirty.replaceAll("ú", "u");

        return dirty;

    }

    public static String lemmatize(String tweet, Lemmatizer lm, StopWords sw){

        String [] temp = tweet.toLowerCase().split("\\s");
        String lemmatized = "";

        for (int i = 0; i<temp.length; i++){
            String clean = clean(temp[i]);
            if ( !sw.isStopWord(clean) && clean.length()!=0 &&
                    clean.length()!=1 && !stillDirty(clean)) {
                String value = lm.get(clean);
                if (value!=null) lemmatized += value+" ";
                else lemmatized += clean+" ";
            }
        }

        return lemmatized.trim();
    }

    public static boolean inArray(String a, String [] b){
        for (int i=0; i<b.length; i++)
            if (a.equals(b[i]))
                return true;
        return false;
    }

    // Busca los nrank tags mas destacados de un cluster
    public static String tags(Dataset ds, int nrank, StopWords sw){

        // Se unen todos los tweets del cluster en un solo string
        String alltweets = "";
        for (int j=0;j<ds.size();j++) {
                    String tweet = (String)ds.get(j).classValue();
                    alltweets = alltweets + tweet + " ";
        }

        // Todas las palabras de todos los tweets
        String [] tws = alltweets.toLowerCase().split("\\s+");
        // Map que relaciona cada string con su numero de ocurrencias
        Map<String, Integer> occurrences = new HashMap<String, Integer>();
        // String de retorno final
        String ret = "";
        // Arreglo con las palabras ya seleccionadas
        String [] selected = new String[nrank];

        // Se crea el Map<String,int> que relaciona cada string con
        // su respectivo numero de ocurrencias (queda desordenado)
        for ( String word : tws ) {
           Integer oldCount = occurrences.get(word);
           if ( oldCount == null ) {
              oldCount = 0;
           }
           occurrences.put(word, oldCount + 1);
        }

        // Se busca el String que tenga mas repeticiones
        String maxs = "";
        int max = 0;
        Iterator it = occurrences.entrySet().iterator();
        while (it.hasNext()) {
                Entry term = (Entry)it.next();
                String word = (String)term.getKey();
                String clean = clean(word.toLowerCase());
                if ( !sw.isStopWord(clean) && clean.length()!=0 &&
                        clean.length()!=1 && !stillDirty(clean)) {
                    int temp = (Integer)term.getValue();
                    if (temp>max) {
                        max = temp;
                        maxs = clean;
                    }
                }
        }
        selected[0] = maxs;
        ret = ret + maxs + "("+max+")";

        // Buscamos el resto de las palabras que nos hacen falta
        for (int i=1; i<nrank; i++){
            max = 0;
            Iterator it2 = occurrences.entrySet().iterator();
            while (it2.hasNext()) {

                Entry term = (Entry)it2.next();
                String word = (String)term.getKey();
                String clean = clean(word.toLowerCase());

                    if ( !sw.isStopWord(clean) && clean.length()!=0 &&
                            clean.length()!=1 && !stillDirty(clean) &&
                            !inArray(clean,selected)) {

                        int temp = (Integer)term.getValue();
                        if (temp>max) {
                            max = temp;
                            maxs = clean;
                        }
                    }
            }
            selected[i] = maxs;
            ret = ret + ", " + maxs + "("+max+")";
        }
        return ret;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Twitter twitter = new TwitterFactory().getInstance();
        Lemmatizer lm = new Lemmatizer("lemas.txt");
        StopWords sw = new StopWords();

        int number_of_clusters = 8;
        int its = 150;
        int tweetsPerAcc = 20;
        String [] accounts = {"trafficaracas", "fmcenter", "trafico", "eutrafico"};

        try {

            String[] tokens;
            TreeSet<String> s = new TreeSet<String>();


            /***************************************************/
            /************** BEGIN VINOTINTO CELAC **************/
            /***************************************************/

            /*QueryResult resultVinotinto = twitter.search(new Query(args[0]).rpp(tweetsPerAcc));
            List<Tweet> tweetsVinotinto = resultVinotinto.getTweets();
            for ( Tweet tweet : resultVinotinto.getTweets()) {
                tokens = tweet.getText().toLowerCase().split("\\s");

                // Cleaning the words and looking for stopwords
                for (int i = 0; i<tokens.length; i++){
                    String clean = clean(tokens[i]);
                    if ( !sw.isStopWord(clean) && clean.length()!=0 &&
                            clean.length()!=1 && !stillDirty(clean)) {
                        String value = lm.get(clean);
                        if (value!=null) s.add(value);
                        else s.add(clean);
                    }
                }
            }

            QueryResult resultCelac = twitter.search(new Query(args[1]).rpp(tweetsPerAcc));
            List<Tweet>  tweetsCELAC = resultCelac.getTweets();
            for ( Tweet tweet : resultCelac.getTweets() ) {
                tokens = tweet.getText().toLowerCase().split("\\s");

                // Cleaning the words and looking for stopwords
                for (int i = 0; i<tokens.length; i++){
                    String clean = clean(tokens[i]);
                    if ( !sw.isStopWord(clean) && clean.length()!=0 &&
                            clean.length()!=1 && !stillDirty(clean)){
                        String value = lm.get(clean);
                        if (value!=null) s.add(value);
                        else s.add(clean);
                    }
                }

            }

            // Removing the empty string from the set of words
            s.remove("");

            System.out.println(s.size());
            //System.out.println(s.toString());

            // Preparar colección
            Token[] col = new Token[tweetsVinotinto.size()
                    + tweetsCELAC.size()];
            int tweetIndex;
            for(tweetIndex = 0; tweetIndex < tweetsVinotinto.size();
                    tweetIndex++) {
                col[tweetIndex] = new Token(tweetsVinotinto.get(tweetIndex),
                        new double[s.size()]);
            }
            for(int j = tweetIndex; j < tweetIndex + tweetsCELAC.size(); j++) {
                col[j] = new Token(tweetsCELAC.get(j % tweetsCELAC.size()),
                        new double[s.size()]);
            }

            // Generando los tweets lematizados
            for (int i = 0; i<col.length; i++){
                col[i].tweetLematized = lemmatize(col[i].tweet.getText(),lm,sw);
            }

            // Term frequency (tf) y Document frequency term
            Iterator<String> it = s.iterator();
            int[] docFreq  = new int[s.size()];
            int setIndex = 0;
            while (it.hasNext()) {

                String term = it.next();

                String value = lm.get(term);
                if (value!=null) term = value;

                for(int i = 0; i < col.length; i++) {
                    boolean present = false;
                    String tweetText = col[i].tweetLematized;
                    int k = 0;
                    while(k < tweetText.length() &&
                            (k = tweetText.indexOf(term, k)) != -1) {
                        col[i].vsm[setIndex]++;
                        present = true;
                        k += term.length();
                    }
                    if (present) {docFreq[setIndex]++;}
                }
                setIndex++;
            }

            // Computar vector de pesos
            for(int i = 0; i < col.length; i++) {
                col[i].computeTermWeight(docFreq,tweetsVinotinto.size()
                        + tweetsCELAC.size(),s);
            }

            // Crear instancias
            Instance[] instances = new Instance[tweetsCELAC.size() +
                    tweetsVinotinto.size()];
            for(int i = 0; i < instances.length; i++) {
                instances[i] = new DenseInstance(col[i].getVsm(),col[i].getTweet().getText());
            }

            /*************************************************/
            /************** END VINOTINTO CELAC **************/
            /*************************************************/




            /*******************************************/
            /************** BEGIN TRAFICO **************/
            /*******************************************/

            Vector<Status> tweets = new Vector<Status>();

            // Se itera sobre todas las cuentas de trafico especificadas
            for (int acc=0; acc<accounts.length; acc++) {

                try {
                    String user = accounts[acc];
                    Paging pag = new Paging(tweetsPerAcc);
                    List<Status> statuses;
                    statuses = twitter.getUserTimeline(user,pag);

                    for (Status status : statuses) {
                        tweets.add(status);
                        tokens = status.getText().toLowerCase().split("\\s");
                        // Cleaning the words and looking for stopwords
                        for (int i = 0; i<tokens.length; i++){
                            String clean = clean(tokens[i]);
                            if ( !sw.isStopWord(clean) && clean.length()!=0 &&
                                    clean.length()!=1 && !stillDirty(clean)) {
                                String value = lm.get(clean);
                                if (value!=null) s.add(value);
                                else s.add(clean);
                            }
                        }
                    }
                } catch (TwitterException te) {
                    te.printStackTrace();
                    System.out.println("Failed to get timeline from "+
                            accounts[acc]+": " + te.getMessage());
                }

            }

            // Removing the empty string from the set of words
            s.remove("");

            System.out.println(s.size());
            //System.out.println(s.toString());

            // Preparar colección
            Token[] col = new Token[tweets.size()];
            int tweetIndex;
            for(tweetIndex = 0; tweetIndex < tweets.size();
                    tweetIndex++) {
                col[tweetIndex] = new Token(tweets.get(tweetIndex),
                        new double[s.size()]);
            }

            // Generando los tweets lematizados
            for (int i = 0; i<col.length; i++){
                col[i].tweetLematized = lemmatize(col[i].tweet.getText(),lm,sw);
            }

            // Term frequency (tf) y Document frequency term
            Iterator<String> it = s.iterator();
            int[] docFreq  = new int[s.size()];
            int setIndex = 0;
            while (it.hasNext()) {

                String term = it.next();

                String value = lm.get(term);
                if (value!=null) term = value;

                for(int i = 0; i < col.length; i++) {
                    boolean present = false;
                    String tweetText = col[i].tweetLematized;
                    int k = 0;
                    while(k < tweetText.length() &&
                            (k = tweetText.indexOf(term, k)) != -1) {
                        col[i].vsm[setIndex]++;
                        present = true;
                        k += term.length();
                    }
                    if (present) {docFreq[setIndex]++;}
                }
                setIndex++;
            }

            // Computar vector de pesos
            for(int i = 0; i < col.length; i++) {
                col[i].computeTermWeight(docFreq,tweets.size(),s);
            }

            // Crear instancias
            Instance[] instances = new Instance[tweets.size()];
            for(int i = 0; i < instances.length; i++) {
                instances[i] = new DenseInstance(col[i].getVsm(),col[i].getTweet().getText());
            }

            /*****************************************/
            /************** END TRAFICO **************/
            /*****************************************/

            Dataset data = new DefaultDataset();
            data.addAll(Arrays.asList(instances));
            Dataset[] best_clusters = null;
            double best_score = 999999999999.0;

            for (int niter=0; niter<1; niter++) {

                Clusterer km = new KMeans(number_of_clusters, its, new CosineSimilarity());
                System.out.println("Clustering...");
                Dataset[] clusters = km.cluster(data);
                System.out.println("End Clustering");

                ClusterEvaluation sse = new SumOfSquaredErrors();
                double score = sse.score(clusters);
                System.out.println("Score = "+score);
                System.out.println("");

                if (score<best_score){
                    best_score = score;
                    best_clusters = clusters;
                }

            }

            System.out.println("Running Clustering with:");
            System.out.println("Number of clusters = "+number_of_clusters);
            System.out.println("Iterations = "+its);
            System.out.println("Distance Measure = Cosine Similarity");
            System.out.println("Best Score = "+best_score);
            System.out.println("");

            System.out.println("Clusters found: " + best_clusters.length);

            for (int i=0;i<number_of_clusters;i++){

                System.out.println("");
                System.out.println("");
                System.out.println(" ========== Cluster #"+(i+1)+" size: "+best_clusters[i].size()+" ========== ");
                System.out.println(" Tags del cluster: "+tags(best_clusters[i],5,sw));
                System.out.println("");
                for (int j=0;j<best_clusters[i].size();j++) {
                    String tweet = (String)best_clusters[i].get(j).classValue();
                    System.out.println(tweet);
                }

            }

            System.out.println("");
            System.out.println(s.toString());
            
        } catch (Exception te) {
            System.out.println("Error: " + te.getMessage());
            System.exit(-1);
        }
    }
}
