/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package documentclustering;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 *
 * @author Lorenzo Fundaró <lorenzofundaro [at] yahoo.com>
 */
public class Documentclustering {

    public static String[] stopwords = {    "a",
                                            "el",
                                            "la",
                                            "lo",
                                            "las",
                                            "les",
                                            "los",
                                            "con",
                                            "de",
                                            "por",
                                            "que",
                                            "me",
                                            "para",
                                            "se",
                                            "un",
                                            "una",
                                            "uno",
                                            "unos",
                                            "unas"
                                            };

    public static boolean isStopword(String term, String[] stopwords){
        for (int i=0;i<stopwords.length;i++)
            // Removing stopwords and links
            if (stopwords[i].compareToIgnoreCase(term)==0 || term.startsWith("http"))
                return true;
        return false;
    }

    public static String clean(String dirty){

        return dirty.replaceAll("[.,;:\"!]", "");

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Twitter twitter = new TwitterFactory().getInstance();

        try {

            String[] tokens;
            TreeSet<String> s = new TreeSet<String>();

            QueryResult resultPrimarias = twitter.search(new Query(args[0]));
            List<Tweet> tweetsPrimarias = resultPrimarias.getTweets();
            for ( Tweet tweet : resultPrimarias.getTweets()) {
                tokens = tweet.getText().toLowerCase().split("\\s");

                // Cleaning the words and looking for stopwords
                for (int i = 0; i<tokens.length; i++){
                    if ( !isStopword(tokens[i],stopwords) && tokens[i].length()!=0)
                        s.add(clean(tokens[i]));
                }


            }
            //                System.out.println(tweet.getText());

            QueryResult resultCelac = twitter.search(new Query(args[1]));
            List<Tweet>  tweetsCELAC = resultCelac.getTweets();
            for ( Tweet tweet : resultCelac.getTweets() ) {
                tokens = tweet.getText().toLowerCase().split("\\s");

                // Cleaning the words and looking for stopwords
                for (int i = 0; i<tokens.length; i++){
                    if ( !isStopword(tokens[i],stopwords) && tokens[i].length()!=0)
                        s.add(clean(tokens[i]));
                }
                
            }

            System.out.println(s.size());

            //            System.out.println(s);
            // Preparar colección
            Token[] col = new Token[tweetsPrimarias.size()
                    + tweetsCELAC.size()];
            int tweetIndex;
            for(tweetIndex = 0; tweetIndex < tweetsPrimarias.size();
                    tweetIndex++) {
                //                System.out.println(tweetsPrimarias.get(i).getText());
                col[tweetIndex] = new Token(tweetsPrimarias.get(tweetIndex),
                        new double[s.size()]);
            }
            for(int j = tweetIndex; j < tweetIndex + tweetsCELAC.size(); j++) {
                col[j] = new Token(tweetsCELAC.get(j % tweetsCELAC.size()),
                        new double[s.size()]);
            }
            //            for(int j = i; j < i + tweetsCELAC.size(); i++) {
            ////                System.out.println(tweetsCELAC.get(i).getText());
            //                col[i] = new Token(tweetsCELAC.get(i),
            //                        new double[s.size()]);
            //            }
            // Term frequency (tf) y Document frequency term
            Iterator<String> it = s.iterator();
            int[] docFreq  = new int[s.size()];
            int setIndex = 0;
            while (it.hasNext()) {

                String term = it.next();

                if (term.length() == 0) {
                    setIndex++;
                    continue;
                }

                boolean present = false;
                for(int i = 0; i < col.length; i++) {
                    String tweetText = col[i].tweet.getText();
                    int k = -1;
                    while(k + term.length() < tweetText.length() &&
                            (k = tweetText.indexOf(term, k+term.length())) != -1) {
                        col[i].vsm[setIndex]++;
                        present = true;
                    }
                    if (present) {docFreq[setIndex]++;}
                }
                setIndex++;
            }
            // Computar vector de pesos
            for(int i = 0; i < col.length; i++) {
                col[i].computeTermWeight(docFreq,tweetsPrimarias.size()
                        + tweetsCELAC.size());
            }
            // Crear instancias
            Instance[] instances = new Instance[tweetsCELAC.size() +
                    tweetsPrimarias.size()];
            for(int i = 0; i < instances.length; i++) {
                instances[i] = new DenseInstance(col[i].getVsm(),col[i].getTweet().getText());
            }
            Dataset data = new DefaultDataset();
            data.addAll(Arrays.asList(instances));
            Clusterer km = new KMeans(2,100);
            Dataset[] clusters = km.cluster(data);
            System.out.println("Cluster count: " + clusters.length);
            //TreeSet<java.lang.Object> p = (TreeSet<java.lang.Object>) clusters[0].classes();
            //TreeSet<java.lang.Object> p2 = (TreeSet<java.lang.Object>) clusters[1].classes();
            //        p.toString();
            
            System.out.println(" ========== Cluster #1 size: "+clusters[0].size());
            //for (int i=0;i<clusters[0].size();i++) System.out.println(clusters[0].get(i).classValue());
            
            System.out.println(" ========== Cluster #2 size: "+clusters[1].size());
            //for (int i=0;i<clusters[1].size();i++) System.out.println(clusters[1].get(i).classValue());

            ClusterEvaluation sse= new SumOfSquaredErrors();
            /* Measure the quality of the clustering */
            double score=sse.score(clusters);
            System.out.println("Score: "+score);

            System.out.println(s.toString());
            
        } catch (TwitterException te) {
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        }
    }
}
