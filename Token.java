/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package documentclustering;

import java.util.TreeSet;
import twitter4j.Status;
import twitter4j.Tweet;

/**
 *
 * @author Lorenzo Fundaró <lorenzofundaro [at] yahoo.com>
 */
public class Token {
    Status tweet;
    String tweetLematized;
    double[] vsm;
    
    public Token(Status t, double[] v) {
        tweet = t;
        vsm = v;
        tweetLematized = "";
    }
    
    public Status getTweet() {
        return tweet;
    }
    
    public double[] getVsm() {
        return vsm;
    }
    
    public void computeTermWeight(int[] dfi, int nD, TreeSet<String> s) {
        for(int i = 0; i < vsm.length; i++) {
            if (dfi[i] > 0) {
                vsm[i] = vsm[i]/(Math.sqrt((double)nD/(double)dfi[i]));
            }
            else if (dfi[i] == 0){
                System.out.println("Error: A document frequency was 0. Check this! This is absurd");
                System.out.println(s.toArray()[i]);
            }
        }
    }

}
