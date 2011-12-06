package documentclustering;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jose
 */
public class Lemmatizer {

    Hashtable lemmas = new Hashtable();

    public Lemmatizer(String filename){

        try{
                FileInputStream fstream = new FileInputStream("lemas.txt");
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                while ((strLine = br.readLine()) != null)   {
                    String [] temp = strLine.split("\\s+");
                    lemmas.put(temp[0], temp[1]);
                }
                in.close();
            } catch (Exception e){
                System.err.println("Error: " + e.getMessage());
            }

    }

    public String get(String key){
        return (String)lemmas.get(key);
    }

}
