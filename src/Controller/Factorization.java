/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Dataset;
import Model.FactorMatrix;
import Model.FactorType;
import Model.Recipe;
import Model.TestMatrix;
import Model.TrainMatrix;
import Model.User;
import Model.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author asus
 */
public class Factorization implements Runnable {
    private List<Recipe> recipes;
    private List<User> users;
    private String chosenUserId;
    private MainDocumentController mdc;
    private TrainMatrix trainM;
    private TestMatrix testM;
    private final int latentSize = 2;
    
   
    public Factorization(MainDocumentController c){
        this.recipes = new ArrayList<Recipe>(); 
        this.users = new ArrayList<User>(); 
        this.mdc = c;
    }
    
    public boolean readRecipesData(){
        boolean success = false;
        try {
            File file = new File("src/data/dataset_recipes_ingredient_list_readable_java.csv");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            String[] tempArr;
            while((line = br.readLine()) != null) {
               tempArr = line.split("[|]+");
               this.recipes.add(new Recipe(tempArr[0],tempArr[1],tempArr[2]));
            }
            br.close();
            success = true;
         } catch(IOException ioe) {
            ioe.printStackTrace();
         } finally {
            return success;
        }
    }
    
    public boolean readUsersData(){
        boolean success = false;
        try {
            File file = new File("src/data/dataset_userId_list.csv");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while((line = br.readLine()) != null) {
               this.users.add(new User(line));
            }
            br.close();
            success = true;
         } catch(IOException ioe) {
            ioe.printStackTrace();
         } finally {
            return success;
        }
    }
    
    public List<Recipe> getRecipes(){return this.recipes;}
    
    public List<User> getUsers(){return this.users;}
    
    public void setUserId(String s){chosenUserId = s;}
    
    public void objectiveFunction(){
        
    }
    
    //creating the first method of factorization (w_o ing   redients matrix)
    @Override
    public void run() {
        this.mdc.insertLog("Starting factorization process \n #1 Reading Dataset\n");
        Dataset d = new Dataset(this.recipes,this.users);
        //1. Read Dataset
        boolean succesReadDataset = d.read();
        if(!succesReadDataset){
            this.mdc.insertLog("Error: Failed to read dataset: dataset_readable_java.csv\n");
            return;
        } else this.mdc.insertLog("Success loading the dataset\n");
        
        this.mdc.insertLog("#2 Splitting Dataset\n");
        //2. Split Dataset
        boolean stg2 = false;
        try {
            List<Object> split_mat = d.split();
            this.trainM = (TrainMatrix) split_mat.remove(0);
            this.testM = (TestMatrix) split_mat.remove(0);
            stg2 = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(stg2) this.mdc.insertLog("Success splitting dataset\n");
            else{
                this.mdc.insertLog("Error: Failed to split dataset\n");
                return;
            }
        }
        
        
        this.mdc.insertLog("#3 Initialize Matrix Factor\n");
        //3. Initialize Matrix Factor  
        boolean stg3 = false;
        try{
            // user factor = m x f
            FactorMatrix userFactor = new FactorMatrix(this.trainM.getRowLength(),this.latentSize,FactorType.USER);
            // recipe factor = f x n
            FactorMatrix recipeFactor = new FactorMatrix(this.latentSize,this.trainM.getColLength(),FactorType.RECIPES);   
            stg3 = true;
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(stg3) this.mdc.insertLog("Success initializing matrix factor dataset\n");
            else{ 
                this.mdc.insertLog("Error: Failed to initialize matrix factor\n");
                return;
            }
        }
        
        List<Pair> trainPair = d.getTrainPair();
        
        this.mdc.insertLog("#4 Entering Optimization Loop process\n");
        //4. entering loop
        do {
            // check the objective function value   
            double objectiveValue = 0.0;  
            for (int i = 0; i < trainPair.size(); i++) {
                    
            }
            
        } while(true); //lefted here
    }   
}
