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
        Dataset data = FileHandler.loadDataset(new File("test2.txt"),4, ",");
        /*
         * Create a new instance of the KMeans algorithm, with no options
         * specified. By default this will generate 4 clusters.
         */
        Clusterer km = new KMeans(3,100);
        /*
         * Cluster the data, it will be returned as an array of data sets, with
         * each dataset representing a cluster
         */
        Dataset[] clusters = km.cluster(data);

        System.out.println("Cluster count: " + clusters.length);

        System.out.println(" === Cluster 1 size: " + clusters[0].size());
        for (int i=0;i<clusters[0].size();i++){
            System.out.println(clusters[0].get(i));
        }
        System.out.println(" === Cluster 2 size: " + clusters[1].size());
        for (int i=0;i<clusters[1].size();i++){
            System.out.println(clusters[1].get(i));
        }
        System.out.println(" === Cluster 3 size: " + clusters[2].size());
        for (int i=0;i<clusters[2].size();i++){
            System.out.println(clusters[2].get(i));
        }

//        TreeSet<java.lang.Object> p = (TreeSet<java.lang.Object>) clusters[1].classes();
//        //        p.toString();
//        System.out.println(p);
////        System.out.println(clusters[3].size());
//        Set<Instance> s = clusters[1].kNearest(1, clusters[1].get(1), new EuclideanDistance());
//        System.out.println(s);
//        ClusterEvaluation sse= new SumOfCentroidSimilarities();
//        /* Measure the quality of the clustering */
//        double score=sse.score(clusters);
//        System.out.println(score);
    }
}
