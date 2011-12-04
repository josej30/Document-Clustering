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
import net.sf.javaml.distance.CosineSimilarity;
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
                                            "unas",
                                            "si",
                                            "tan",
                                            "te",
                                            "y",
                                            "o",
                                            "u",
                                            "q",
                                            "ni",
                                            "no",
                                            "rt"
                                            };

    public static boolean isStopword(String term, String[] stopwords){
        for (int i=0;i<stopwords.length;i++)
            // Removing stopwords and links
            if (stopwords[i].compareToIgnoreCase(term)==0 || 
                term.startsWith("http") || term.startsWith("@"))
                return true;
        return false;
    }

    public static boolean stillDirty(String term){
        if (term.matches("[.,;:@\"!/()]"))
            return true;
        return false;
    }

    public static String clean(String dirty){

        dirty = dirty.replaceAll("^[.,;:\"!/()]+", "");
        dirty = dirty.replaceAll("[.,;:\"!/()]+$", "");

        return dirty;

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Twitter twitter = new TwitterFactory().getInstance();

        int number_of_clusters = 4;
        int its = 1000;
        int tweets_per_search = 100;

        try {

            String[] tokens;
            TreeSet<String> s = new TreeSet<String>();

            QueryResult resultVinotinto = twitter.search(new Query(args[0]).rpp(tweets_per_search));
            List<Tweet> tweetsVinotinto = resultVinotinto.getTweets();
            for ( Tweet tweet : resultVinotinto.getTweets()) {
                tokens = tweet.getText().toLowerCase().split("\\s");

                // Cleaning the words and looking for stopwords
                for (int i = 0; i<tokens.length; i++){
                    String clean = clean(tokens[i]);
                    if ( !isStopword(clean,stopwords) && clean.length()!=0 && 
                            clean.length()!=1 && !stillDirty(clean))
                        s.add(clean);
                }


            }
            //                System.out.println(tweet.getText());

            QueryResult resultCelac = twitter.search(new Query(args[1]).rpp(tweets_per_search));
            List<Tweet>  tweetsCELAC = resultCelac.getTweets();
            for ( Tweet tweet : resultCelac.getTweets() ) {
                tokens = tweet.getText().toLowerCase().split("\\s");

                // Cleaning the words and looking for stopwords
                for (int i = 0; i<tokens.length; i++){
                    String clean = clean(tokens[i]);
                    if ( !isStopword(clean,stopwords) && clean.length()!=0 &&
                            clean.length()!=1 && !stillDirty(clean))
                        s.add(clean);
                }
                
            }

            // Removing the empty string from the set of words
            s.remove("");

            System.out.println(s.size());


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

            // Term frequency (tf) y Document frequency term
            Iterator<String> it = s.iterator();
            int[] docFreq  = new int[s.size()];
            int setIndex = 0;
            while (it.hasNext()) {

                String term = it.next();

                for(int i = 0; i < col.length; i++) {
                    boolean present = false;
                    String tweetText = col[i].tweet.getText().toLowerCase();
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
            Dataset data = new DefaultDataset();
            data.addAll(Arrays.asList(instances));
            Clusterer km = new KMeans(number_of_clusters, its, new CosineSimilarity());
            Dataset[] clusters = km.cluster(data);

            System.out.println("Running Clustering with:");
            System.out.println("Number of clusters = "+number_of_clusters);
            System.out.println("Iterations = "+its);
            System.out.println("Distance Measure = Cosine Similarity");
            System.out.println("");

            System.out.println("Clusters found: " + clusters.length);

            for (int i=0;i<number_of_clusters;i++){

                int celac = 0;
                int vinotinto = 0;
                System.out.println(" ========== Cluster #"+i+" size: "+clusters[i].size());
                for (int j=0;j<clusters[i].size();j++) {
                    String tweet = (String)clusters[i].get(j).classValue();
                    if (tweet.matches("(?i).*#celac.*"))
                      celac++;
                    if (tweet.matches("(?i).*#vinotinto.*"))
                      vinotinto++;
                    System.out.println(tweet);
                }
                System.out.println(celac + " tweets con #CELAC");
                System.out.println(vinotinto + " tweets con #VINOTINTO");

            }

            System.out.println(s.toString());

            /* Create a measure for the cluster quality */
            ClusterEvaluation sse= new SumOfSquaredErrors();
            /* Measure the quality of the clustering */
            double score=sse.score(clusters);

            System.out.println("Score: "+score);
            
        } catch (TwitterException te) {
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        }
    }
}
