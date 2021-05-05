/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Dataset;
import Model.FactorMatrix;
import Model.FactorType;
import Model.MatrixUtil;
import Model.Recipe;
import Model.TestMatrix;
import Model.TrainMatrix;
import Model.User;
import Model.Pair;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    private double[][] modelRes;
    private final int LATENT_SIZE = 2;
    private final double LAMBDA = 0.1;
    private final int MAX_LOOP = 1000;
    private final double MIN_OBJECTIVE_VAL = 10.0;
    private final double LEARNING_RATE = 0.0001;
    
    
    //
    private List<Pair> trainPair;
    private HashMap<String, Integer> userMap;
    private HashMap<String, Integer> recipeMap;
    private HashMap<Integer, String> userMap_r;
    private HashMap<Integer, String> recipeMap_r;
   
    public Factorization(MainDocumentController c){
        this.recipes = new ArrayList<Recipe>(); 
        this.users = new ArrayList<User>(); 
        this.mdc = c;
        this.trainPair = null;
        this.userMap = null;
        this.userMap_r = null;
        this.recipeMap = null;
        this.recipeMap_r = null;
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
    
    public double objectiveFunction(FactorMatrix user, FactorMatrix recipe){
        double sumOfErrorSquared = 0.0;
        for (int i = 0; i < trainPair.size(); i++) {
            Pair curr = trainPair.get(i);
            String[] p = curr.getPair();
            int userIndex = userMap.get(p[0]);
            int recipeIndex = recipeMap.get(p[1]);
            double[] userFactor = user.getFactorByIndex(userIndex);
            double[] recipeFactor = recipe.getFactorByIndex(recipeIndex);
            double predicted = MatrixUtil.vectorMultiplication(userFactor, recipeFactor);
            sumOfErrorSquared += Math.pow((trainM.getEntryByIndex(userIndex, recipeIndex) - predicted),2);
        }        
        
        double penalty = LAMBDA*(user.calculateVectorLength()+recipe.calculateVectorLength());
        return sumOfErrorSquared + penalty;
    }
    
    public void alternatingGradientDescent(FactorMatrix user, FactorMatrix recipe){
        //update the user matrix factor value
        double[][] userVector = user.getEntry();
        for (int i = 0; i < userVector.length; i++) {
//            MatrixUtil.print(userVector[i]);
            userVector[i] = MatrixUtil.vectorCalculation(
                    userVector[i], //previous value
                    calculateLearningValue(userVector[i], 
                            i, recipe, trainPair, 
                            userMap_r, FactorType.USER), //learning value
                    1); //addition
//            System.out.print("#");
//            MatrixUtil.print(userVector[i]);
        }
        //update the recipe matrix factor value
        double[][] matrixVector = recipe.getEntry();
        for (int i = 0; i < matrixVector.length; i++) {
            matrixVector[i] = MatrixUtil.vectorCalculation(
                    matrixVector[i], //previous value
                    calculateLearningValue(matrixVector[i], 
                            i, user, trainPair, 
                            recipeMap_r, FactorType.RECIPES), //learning value
                    1); //addition
        }
    }
    
    public double[] calculateLearningValue(double[] latentVector, int index, FactorMatrix fm,
            List<Pair> observable, HashMap<Integer, String> reverseMap, FactorType type){
        String targetId = reverseMap.get(index);
        double[] res = new double[latentVector.length];
        List<Pair> filtered = null;
        switch(type){
            case USER:
                filtered = observable.stream().filter(p -> p.getUser().equals(targetId)).collect(Collectors.toList());
//                System.out.println(filtered.size());
                for (int i = 0; i < filtered.size(); i++) {
                    int recipe_index = recipeMap.get(filtered.get(i).getRecipe());
                    double[] pairLatent = fm.getFactorByIndex(recipe_index);
                    double trainValue = trainM.getEntryByIndex(index, recipe_index);
                    double error = trainValue - MatrixUtil.vectorMultiplication(latentVector, pairLatent);
//                    System.out.print("index: "+recipe_index+" | ");
//                    System.out.print("pair latent: ");
//                    for (int j = 0; j < pairLatent.length; j++) {
//                        System.out.print(pairLatent[j]+", ");
//                    }
//                    System.out.print(" | train value:"+trainValue+" | ");
//                    System.out.print("error: "+error);
                    //calculate error times pair latent
                    double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pairLatent);
                    //calculate lambda times targeted latent vector
                    double[] lambdaTarget = MatrixUtil.scalarMultiplication(LAMBDA, latentVector);
                    //calculate vector result from current observable pair
                    double[] currVector = MatrixUtil.vectorCalculation(
                            errorTimesPair, 
                            lambdaTarget, 
                            0);
                    res = MatrixUtil.vectorCalculation(res, currVector, 1);
                }
//                System.out.println("");
                break;
            case RECIPES:
                filtered = observable.stream().filter(p -> p.getRecipe().equals(targetId)).collect(Collectors.toList());
                for (int i = 0; i < filtered.size(); i++) {
                    int user_index = userMap.get(filtered.get(i).getUser());
                    double[] pairLatent = fm.getFactorByIndex(user_index);
                    double trainValue = trainM.getEntryByIndex(user_index, index);
                    double error = trainValue - MatrixUtil.vectorMultiplication(latentVector, pairLatent);
                    //calculate error times pair latent
                    double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pairLatent);
                    //calculate lambda times targeted latent vector
                    double[] lambdaTarget = MatrixUtil.scalarMultiplication(LAMBDA, latentVector);
                    //calculate vector result from current observable pair
                    double[] currVector = MatrixUtil.vectorCalculation(
                            errorTimesPair, 
                            lambdaTarget, 
                            0);
                    res = MatrixUtil.vectorCalculation(res, currVector, 1);
                }
                break;
            case INGREDIENTS:
                break;
            default:
                break;
        }
        //calculate result * learning rate
        res = MatrixUtil.scalarMultiplication(LEARNING_RATE, res);
        return res;
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
        FactorMatrix userFactor = null;
        FactorMatrix recipeFactor = null;
        try{
            // user factor = m x f
            userFactor = new FactorMatrix(this.trainM.getRowLength(),this.LATENT_SIZE,FactorType.USER);
            // recipe factor = n x f
            recipeFactor = new FactorMatrix(this.trainM.getColLength(),this.LATENT_SIZE,FactorType.RECIPES);   
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
        
        //set attribute
        this.trainPair = d.getTrainPair();
        this.userMap = d.getUserMap();
        this.recipeMap = d.getRecipeMap();
        this.userMap_r = d.getReverseUserMap();
        this.recipeMap_r = d.getReverseRecipeMap();
        
        this.mdc.insertLog("#4 Entering Optimization Loop process\n"
                + "#######\n"
                + "# Process Detail:\n"
                + "# Max Number of iteration: "+MAX_LOOP+"\n"
                + "#######\n"
        );
        //4. entering loop  
        //loop a thousand time or break if objective value less than MIN_OBJECTIVE_VAL
        boolean stg4 = false;
        try{
            int loop = MAX_LOOP;
            while(loop-->0){
                System.out.println("loop: "+(1000-loop));
                double objectiveValue = objectiveFunction(userFactor,recipeFactor);
                System.out.println(objectiveValue);
//                this.mdc.insertLog("Iteration number: "+(1000-loop)+"\n");
//                this.mdc.insertLog(String.format("Objective Function Value = %.2f\n",objectiveValue));
                if(objectiveValue <= MIN_OBJECTIVE_VAL) break;
                else if(Double.isNaN(objectiveValue)) throw new RuntimeException("Objective Function value is NaN");
                else{
                    //do A-GD
                    alternatingGradientDescent(userFactor,recipeFactor);
                }   
            }
            stg4 = true;
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        } finally {
            if(stg4) this.mdc.insertLog("Optimization process done\n");
            else{ 
                this.mdc.insertLog("Error: Failure occured in optimization process\n");
                return;
            }
        }
        
        //check model result
        this.modelRes = userFactor.mulitply(recipeFactor.transpose());
//        List<String[]> matrix_csv = new ArrayList<String[]>();
//        for (int i = 0; i < modelRes.length; i++) {
//            matrix_csv.add(new String[modelRes[i].length]);
//            String[] curr = matrix_csv.get(0);
//            for (int j = 0; j < curr.length; j++) {
//                curr[j] = String.format("%.3f",modelRes[i][j]);
//            }
//        }
//        
//        try{
//            Path path = Paths.get(
//                ClassLoader.getSystemResource("csv/model.csv").toURI()); 
//           
//            CSVWriter writer = new CSVWriter(new FileWriter(path.toString()));
//            writer.writeAll(matrix_csv);
//            writer.close();
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//        this.mdc.insertLog("Model saved in csv\n");
    }   
    
    public double[][] getModel(){return this.modelRes;}
}
