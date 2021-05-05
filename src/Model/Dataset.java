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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author asus
 */
public class Dataset extends Matrix {
    private HashMap<String, Integer> recipeMap;
    private HashMap<Integer, String> recipeMap_r;
    private HashMap<String, Integer> userMap;
    private HashMap<Integer, String> userMap_r;
    private List<Pair> userRecipe_pair; //List of Pair for train matrix
    private List<Pair> maskPair; //List of Pair for test matrix
    
    public Dataset(List<Recipe> r, List<User> u){
        super(u.size(),r.size());
        this.recipeMap = new HashMap<String, Integer>();
        this.recipeMap_r = new HashMap<Integer, String>();
        this.userMap = new HashMap<String, Integer>();
        this.userMap_r = new HashMap<Integer, String>();
        this.userRecipe_pair = new ArrayList<Pair>();
        this.maskPair = new ArrayList<Pair>();
        int index = 0;
        for(Recipe o: r){
            recipeMap.put(o.getId(), index);
            recipeMap_r.put(index, o.getId());
            index++;
        }
        index = 0;
        for(User o: u){
            userMap.put(o.getId(), index);
            userMap_r.put(index, o.getId());
            index++;
        }
    }
    
    public boolean read(){
        boolean success = false;
        try {
            File file = new File("src/data/dataset_readable_java.csv");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
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
    
    public List<Object> split(){
        // thanks: https://stackoverflow.com/questions/5617016/how-do-i-copy-a-2-dimensional-array-in-java
        double[][] trainMatrix = Arrays.stream(this.entry).map(double[]::clone).toArray(double[][]::new);
        
        TrainMatrix tm = new TrainMatrix(trainMatrix);
        TestMatrix _tm = new TestMatrix(this.entry.length, this.entry[0].length);
        
        //Splitting the list using 8:2 ratio for train:test
        int sizeMask =  20*this.userRecipe_pair.size()/100;
        Random r = new Random();
        for (int i = 0; i < sizeMask; i++) {
            int rIndex = r.nextInt(this.userRecipe_pair.size());
            Pair p = this.userRecipe_pair.remove(rIndex);
            String[] pair = p.getPair();
            int row_idx = userMap.get(pair[0]);
            int col_idx = recipeMap.get(pair[1]);
            
            maskPair.add(p);
            
            //apply masking, for faster coding
            //set train matrix to 0
            tm.changeEntry(row_idx,col_idx, 0);
            //set test matrix entry to value
            _tm.changeEntry(row_idx,col_idx, this.entry[row_idx][col_idx]);
        }
        
        List<Object> result = new LinkedList<Object>(Arrays.asList(tm,_tm));
        return result;
    }
    
    public List<Pair> getTrainPair(){return this.userRecipe_pair;}
    public List<Pair> getTestPair(){return this.maskPair;}
    public HashMap<String, Integer> getRecipeMap(){return this.recipeMap;}
    public HashMap<String, Integer> getUserMap(){return this.userMap;}
    public HashMap<Integer, String> getReverseRecipeMap(){return this.recipeMap_r;}
    public HashMap<Integer, String> getReverseUserMap(){return this.userMap_r;}
}
