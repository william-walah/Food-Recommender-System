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
import Model.RecipePredicted;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private final double LAMBDA = 1;
    private final int MAX_LOOP = 1000;
    private final double MIN_OBJECTIVE_VAL = 10.0;
    private final double LEARNING_RATE = 0.0001;
    
    
    //
    private List<Pair> trainPair;
    private HashMap<String, Integer> userMap;     //memetakan ID user kepada index matrix user & list user
    private HashMap<String, Integer> recipeMap;   //memetakan ID resep kepada index matrix resep & list resep
    private HashMap<Integer, String> userMap_r;   //memetakan index matrix / list user dengan ID user
    private HashMap<Integer, String> recipeMap_r; //memetakan index matriks / list resep dengan ID resep
   
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
            //truncated value so it lays between 0-5
            //current latent factor length = 2
            //possible value is 5*5 + 5*5 = 50, so its divided by 10
            predicted = predicted/10;
            //System.out.println(String.format("%.3f_p %.3f_a",predicted,trainM.getEntryByIndex(userIndex, recipeIndex)));
            sumOfErrorSquared += Math.pow((trainM.getEntryByIndex(userIndex, recipeIndex) - predicted),2);
        }        
        
//        System.out.println(String.format("%.3f",sumOfErrorSquared));
        double penalty = LAMBDA*(user.calculateVectorLength()+recipe.calculateVectorLength());
//        System.out.println(String.format("%.3f",penalty));
        return sumOfErrorSquared + penalty;
    }
    
    public void alternatingGradientDescent(FactorMatrix user, FactorMatrix recipe){
        //update the user matrix factor value
        double[][] userVector = user.getEntry();
        for (int i = 0; i < userVector.length; i++) {
            double[] newLatent = MatrixUtil.vectorCalculation(
                    userVector[i], //previous value
                    calculateLearningValue(userVector[i], 
                            i, recipe, trainPair, 
                            userMap_r, FactorType.USER), //learning value
                    1); //addition
            //um... ubah value kalau lebih dari 5 jadi 5, kurang dari 0 jadi 0?
            for (int j = 0; j < newLatent.length; j++) {
//                System.out.print(newLatent[j]+", ");
                if(newLatent[j] > 5) newLatent[j] = 5.0;
                else if(newLatent[j] < 0) newLatent[j] = 0.0;
//                System.out.println(newLatent[j]);
            }
            userVector[i] = newLatent;
        }
        //update the recipe matrix factor value
        double[][] recipeVetor = recipe.getEntry();
        for (int i = 0; i < recipeVetor.length; i++) {
            double[] newLatent = MatrixUtil.vectorCalculation(
                    recipeVetor[i], //previous value
                    calculateLearningValue(recipeVetor[i], 
                            i, user, trainPair, 
                            recipeMap_r, FactorType.RECIPES), //learning value
                    1); //addition
            for (int j = 0; j < newLatent.length; j++) {
//                System.out.print(newLatent[j]+", ");
                if(newLatent[j] > 5) newLatent[j] = 5.0;
                else if(newLatent[j] < 0) newLatent[j] = 0.0;
//                System.out.println(newLatent[j]);
            }
            recipeVetor[i] = newLatent;
        }
    }
    
    public double[] calculateLearningValue(double[] latentVector, int index, FactorMatrix fm,
            List<Pair> observable, HashMap<Integer, String> reverseMap, FactorType type){
        
        String targetId = reverseMap.get(index);
        double[] res = new double[latentVector.length];
        double[] lambdaTarget = null;
        List<Pair> filtered = null;
        
        switch(type){
            case USER:
                filtered = observable.stream().filter(p -> p.getUser().equals(targetId)).collect(Collectors.toList());
                //calculate lambda times targeted latent vector
                lambdaTarget = MatrixUtil.scalarMultiplication(LAMBDA, latentVector);
                for (int i = 0; i < filtered.size(); i++) {
                    int recipe_index = recipeMap.get(filtered.get(i).getRecipe());
                    double[] pairLatent = fm.getFactorByIndex(recipe_index);
                    double trainValue = trainM.getEntryByIndex(index, recipe_index);
                    double error = trainValue - (MatrixUtil.vectorMultiplication(latentVector, pairLatent)/10);
                    //calculate error times pair latent
                    double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pairLatent);
                    //calculate vector result from current observable pair
                    double[] currVector = MatrixUtil.vectorCalculation(
                            errorTimesPair, 
                            lambdaTarget, 
                            0);
                    res = MatrixUtil.vectorCalculation(res, currVector, 1);
                }
                res = MatrixUtil.vectorCalculation(res, lambdaTarget, 0);
                break;
            case RECIPES:
                filtered = observable.stream().filter(p -> p.getRecipe().equals(targetId)).collect(Collectors.toList());
                //calculate lambda times targeted latent vector
                lambdaTarget = MatrixUtil.scalarMultiplication(LAMBDA, latentVector);
                for (int i = 0; i < filtered.size(); i++) {
                    int user_index = userMap.get(filtered.get(i).getUser());
                    double[] pairLatent = fm.getFactorByIndex(user_index);
                    double trainValue = trainM.getEntryByIndex(user_index, index);
                    double error = trainValue - (MatrixUtil.vectorMultiplication(latentVector, pairLatent)/10);
                    //calculate error times pair latent
                    double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pairLatent);
                    //calculate vector result from current observable pair
                    double[] currVector = MatrixUtil.vectorCalculation(
                            errorTimesPair, 
                            lambdaTarget, 
                            0);
                    res = MatrixUtil.vectorCalculation(res, currVector, 1);
                }
                res = MatrixUtil.vectorCalculation(res, lambdaTarget, 0);
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
    
    public double rmse(Dataset d){ //double[][] prediction and TestMatrix
        System.out.println("bee boop dipslaying rmse process");
        List<Pair> testPair = d.getTestPair();
        double squaredError = 0.0;
        for (int i = 0; i < testPair.size(); i++) {
            Pair curr = testPair.get(i);
            int userIndex = this.userMap.get(curr.getUser());
            int recipeIndex = this.recipeMap.get(curr.getRecipe());
            double predicted = this.modelRes[userIndex][recipeIndex] / 10; //divide by ten because latent length = 2, max entry value = 5, thus 5*2
            double actual = this.testM.getEntryByIndex(userIndex, recipeIndex);
            System.out.println(String.format("%d. Predicted = %.2f, Actual = %.2f", (i+1), predicted, actual));
            if(actual < 1) throw new RuntimeException("Missing value in test matrix");
            squaredError += Math.pow((predicted-actual),2);
        }
        return Math.sqrt(squaredError/(double) testPair.size());
    }
    
    public String[] topTenRecipe(){
        int userIndex = this.userMap.get(this.chosenUserId);
        List<RecipePredicted> l = new ArrayList<RecipePredicted>();
        for (int i = 0; i < modelRes[userIndex].length; i++) {
            l.add(new RecipePredicted(modelRes[userIndex][i],i));
        }
        Collections.sort(l);
        String[] res = new String[10];
        for (int i = 0; i < 10; i++) {
            RecipePredicted curr = l.get(i);
            String temp = "# "+(i+1)+". "+this.recipes.get(curr.getIndex()).getName() 
                    +" ("+String.format("%.2f",curr.getValue()/10)+")";
            
            res[i] = temp;
        }
        return res;
    }
    
    //creating the first method of factorization (w_o ing   redients matrix)
    @Override
    public void run() {
        this.mdc.insertLog("Starting factorization process \n#1 Reading Dataset\n");
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
//                if(loop<=750) LEARNING_RATE /= 10;
//                else if(loop<=500) LEARNING_RATE /= 10;
//                else if(loop<=250) LEARNING_RATE /= 10;
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
        List<String[]> matrix_csv = new ArrayList<String[]>();
        for (int i = 0; i < modelRes.length; i++) {
            matrix_csv.add(new String[modelRes[i].length]);
            String[] curr = matrix_csv.get(0);
            for (int j = 0; j < curr.length; j++) {
                curr[j] = String.format("%.3f",modelRes[i][j]);
            }
        }
        try{
            //File f = new File(getClass().getResource("/"))
            CSVWriter writer = new CSVWriter(new FileWriter("C:\\Users\\asus\\Desktop\\model.csv", true));
            for (String[] s: matrix_csv) {
                writer.writeNext(s);
            }
            writer.close();
            this.mdc.insertLog("Model saved in csv\n");
        } catch(Exception e) {
            e.printStackTrace();
        } 
        
        
        double rmse = rmse(d);
        String[] top10 = topTenRecipe();
        String str = String.join("\n",top10);
        
        this.mdc.insertLog("Result:\n"+String.format("# RMSE = %.3f\n",rmse));
        this.mdc.insertLog("# User ID: "+this.chosenUserId+"\n"
                + "# Top 10 Recipe Recommendation: \n"
                + str +"\n");
        this.mdc.insertLog("\nFactorization Done.");
        
    }   
    
    public double[][] getModel(){return this.modelRes;}
}
