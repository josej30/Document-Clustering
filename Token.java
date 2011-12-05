/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package documentclustering;

import java.util.TreeSet;
import twitter4j.Tweet;

/**
 *
 * @author Lorenzo Fundaró <lorenzofundaro [at] yahoo.com>
 */
public class Token {
    Tweet tweet;
    double[] vsm;
    String fromUser;
    
    public Token(Tweet t, double[] v) {
        tweet = t;
        vsm = v;
        fromUser = t.getFromUser();
    }
    
    public Tweet getTweet() {
        return tweet;
    }
    
    public double[] getVsm() {
        return vsm;
    }

    public String getFromUser() {
        return fromUser;
    }
    
    public void computeTermWeight(int[] dfi, int nD, TreeSet<String> s) {
        for(int i = 0; i < vsm.length; i++) {
            if (dfi[i] > 0) {
                vsm[i] = vsm[i];//*(Math.log((double)nD/(double)dfi[i]));
            }
            else if (dfi[i] == 0){
                System.out.println("Error: A document frequency was 0. Check this! This is absurd");
            }
        }
    }

}
