/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clusteringtest;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.data.FileHandler;

/**
 *
 * @author Lorenzo Fundaró <lorenzofundaro [at] yahoo.com>
 */
public class ClusteringTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        /* Load a dataset */
        Dataset data = FileHandler.loadDataset(new File("test.txt"),11, ",");
        /*
         * Create a new instance of the KMeans algorithm, with no options
         * specified. By default this will generate 4 clusters.
         */
        Clusterer km = new KMeans(2,100);
        /*
         * Cluster the data, it will be returned as an array of data sets, with
         * each dataset representing a cluster
         */
        Dataset[] clusters = km.cluster(data);
        System.out.println("Cluster count: " + clusters.length);
        TreeSet<java.lang.Object> p = (TreeSet<java.lang.Object>) clusters[1].classes();
        //        p.toString();
        System.out.println(p);
//        System.out.println(clusters[3].size());
        Set<Instance> s = clusters[1].kNearest(1, clusters[1].get(1), new EuclideanDistance());
        System.out.println(s);
//        ClusterEvaluation sse= new SumOfCentroidSimilarities();
//        /* Measure the quality of the clustering */
//        double score=sse.score(clusters);
//        System.out.println(score);
    }
}
