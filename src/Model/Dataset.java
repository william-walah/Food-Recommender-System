/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author asus
 */
public class Dataset extends Matrix {
    private List<Pair> userRecipe_pair; //List of Pair matrix
    private List<Pair> trainPair; //List of Pair for train matrix
    private List<Pair> testPair; //List of Pair for test matrix
    
    public Dataset(int userSize, int recipeSize){
        super(userSize,recipeSize);
        this.userRecipe_pair = new ArrayList<Pair>();
        this.trainPair = new ArrayList<Pair>();
        this.testPair = new ArrayList<Pair>();
    }
    
    //for copy purpose
    public Dataset(double[][] entry){
        super(entry);
        this.userRecipe_pair = new ArrayList<Pair>();
        this.trainPair = new ArrayList<Pair>();
        this.testPair = new ArrayList<Pair>();
    }
    
    public Dataset copy(){
        Dataset copy = new Dataset(this.entry);
        List<Pair> dataPair = new ArrayList<Pair>();
        for(Pair curr: this.userRecipe_pair){
            dataPair.add(new Pair(curr.getUser(),curr.getRecipe()));
        }
        copy.setDataPair(dataPair);
        return copy;
    }
    
    public boolean read(
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap
    ){
        boolean success = false;
        try {
            InputStream in = getClass().getResourceAsStream("/data/dataset_readable_java.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            String[] tempArr;
            while((line = br.readLine()) != null) {
               tempArr = line.split(",");
               int row_idx = userMap.get(tempArr[0]);
                for (int i = 1; i < tempArr.length; i++) {
                    String[] recipe_rating = tempArr[i].split("_");
                    int col_idx = recipeMap.get(recipe_rating[0]);
                    //add new entry
                    this.entry[row_idx][col_idx] = Double.parseDouble(recipe_rating[1]);
                    //add new pair for masking purpose
                    this.userRecipe_pair.add(new Pair(tempArr[0],recipe_rating[0]));
                }
            }
            br.close();
            success = true;
         } catch(IOException ioe) {
            ioe.printStackTrace();
         } finally {
            return success;
        }
    }
    
    public List<Object> split(
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap
    ){
        // thanks: https://stackoverflow.com/questions/5617016/how-do-i-copy-a-2-dimensional-array-in-java
        double[][] trainMatrix = Arrays.stream(this.entry).map(double[]::clone).toArray(double[][]::new);
        
        TrainMatrix tm = new TrainMatrix(trainMatrix);
        TestMatrix _tm = new TestMatrix(this.entry.length, this.entry[0].length);
        
        //Splitting the list using 8:2 ratio for train:test
        int sizeMask =  20*this.userRecipe_pair.size()/100;
        Random r = new Random();
        
        //reset attribute
        this.trainPair.clear();
        this.testPair.clear();
        
        // deep copy the dataset pair to train pair
        for(Pair curr: this.userRecipe_pair){
            this.trainPair.add(new Pair(curr.getUser(),curr.getRecipe()));
        }
        for (int i = 0; i < sizeMask; i++) {
            int rIndex = r.nextInt(this.trainPair.size());
            Pair p = this.trainPair.remove(rIndex);
            int row_idx = userMap.get(p.getUser());
            int col_idx = recipeMap.get(p.getRecipe());
            
            this.testPair.add(p);
            
            //apply masking, for faster coding
            //set train matrix to 0
            tm.changeEntry(row_idx,col_idx, 0);
            //set test matrix entry to value
            _tm.changeEntry(row_idx,col_idx, this.entry[row_idx][col_idx]);
        }
        System.out.println(userRecipe_pair.size());
        System.out.println(trainPair.size());
        System.out.println(testPair.size());
        List<Object> result = new LinkedList<Object>(Arrays.asList(tm,_tm));
        return result;
    }
    
    public void addNewVector(double[] v, List<Pair> customPair){
        double[][] newEntry = new double[this.entry.length+1][this.entry[0].length];
        for (int i = 0; i < newEntry.length-1; i++) {
            newEntry[i] = this.entry[i];
        }
        newEntry[newEntry.length-1] = v;
        this.entry = newEntry;
        this.userRecipe_pair.addAll(customPair);
    }
    
    public List<Pair> getDataPair(){return this.userRecipe_pair;}
    public List<Pair> getTrainPair(){return this.trainPair;}
    public List<Pair> getTestPair(){return this.testPair;}
    public void setDataPair(List<Pair> o){this.userRecipe_pair = o;};
    public void setTrainPair(List<Pair> o){this.trainPair = o;};
    public void setTestPair(List<Pair> o){this.testPair = o;};
}
