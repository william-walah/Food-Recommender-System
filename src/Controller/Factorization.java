/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Dataset;
import Model.FactorMatrix;
import Model.FactorType;
import Model.FactorizationUtil;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 *
 * @author asus
 */
public class Factorization implements Runnable {
    private List<Recipe> recipes;
    private List<User> users;
    private List<String> ingredients;
    private String chosenUserId;
    private MainDocumentController mdc;
    private TrainMatrix trainM;
    private TestMatrix testM; 
    private double[][] modelRes;
    private final int MAX_LOOP = 1000;
    private final double MIN_OBJECTIVE_VAL = 10.0;
    
    //
    private List<Pair> trainPair;
    private HashMap<String, Integer> userMap;     //memetakan ID user kepada index matrix user & list user
    private HashMap<String, Integer> recipeMap;   //memetakan ID resep kepada index matrix resep & list resep
    private HashMap<String, Integer> ingredientMap;
    private HashMap<Integer, String> userMap_r;   //memetakan index matrix / list user dengan ID user
    private HashMap<Integer, String> recipeMap_r; //memetakan index matriks / list resep dengan ID resep
    private HashMap<Integer, String> ingredientMap_r;
    
    public Factorization(MainDocumentController c){
        this.recipes = new ArrayList<Recipe>(); 
        this.users = new ArrayList<User>(); 
        this.ingredients = new ArrayList<String>();
        this.ingredientMap = new HashMap<String,Integer>();
        this.ingredientMap_r = new HashMap<Integer,String>();
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
            Set<String> ingredientsUniqueId = new HashSet<String>();
            while((line = br.readLine()) != null) {
               tempArr = line.split("[|]+");
               this.recipes.add(new Recipe(tempArr));
               ingredientsUniqueId.addAll(Arrays.asList(tempArr[3].split(",")));
            }
            br.close();
            int index = 0;
            for(String uniqueId: ingredientsUniqueId){
                this.ingredients.add(uniqueId);
                this.ingredientMap.put(uniqueId,index);
                this.ingredientMap_r.put(index,uniqueId);
                index++;
            }
            success = true;
         } catch(IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
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
        
        //set attribute
        this.trainPair = d.getTrainPair();
        this.userMap = d.getUserMap();
        this.recipeMap = d.getRecipeMap();
        this.userMap_r = d.getReverseUserMap();
        this.recipeMap_r = d.getReverseRecipeMap();
        FactorizationUtil utils = new FactorizationUtil(this.ingredients.size());
        
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
        FactorMatrix userFactor_2 = null;
        FactorMatrix recipeFactor = null;
        FactorMatrix ingredientFactor = null;
        FactorMatrix recipeIngredientsMap = null;
        try{
            // user factor = m x f
            userFactor = new FactorMatrix(this.trainM.getRowLength(),utils.getLatentSize(),FactorType.USER);
            // recipe factor = n x f
            recipeFactor = new FactorMatrix(this.trainM.getColLength(),utils.getLatentSize(),FactorType.RECIPES);   
            // ingredient factor = o x f
            ingredientFactor = new FactorMatrix(this.ingredients.size(),utils.getLatentSize(),FactorType.INGREDIENTS);
            // recipe x ingredient map matrix = n x o (mask matrix 1/0)
            recipeIngredientsMap = new FactorMatrix(this.trainM.getColLength(),
                    this.ingredients.size(),
                    FactorType.RECIPE_ING_MAP, 
                    this.recipes, 
                    this.recipeMap,
                    this.ingredientMap);
            userFactor_2 = new FactorMatrix(userFactor);
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
                double objectiveValue = utils.objectiveFunction_m1(
                        userFactor,
                        recipeFactor,
                        this.trainM,
                        this.trainPair,
                        this.userMap,
                        this.recipeMap
                );
                System.out.println(objectiveValue);
                if(objectiveValue <= MIN_OBJECTIVE_VAL) break;
                else if(Double.isNaN(objectiveValue)) throw new RuntimeException("Objective Function value is NaN");
                else{
                    //do A-GD
                    utils.alternatingGradientDescent_m1(
                            userFactor,
                            recipeFactor,
                            this.trainPair,
                            this.userMap,
                            this.recipeMap,
                            this.userMap_r,
                            this.recipeMap_r,
                            this.trainM
                    );
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
        
        
        double rmse = utils.rmse(
                d.getTestPair(),
                this.userMap,
                this.recipeMap,
                this.modelRes,
                this.testM
        );
        String[] top10 = utils.topTenRecipe(
                this.recipes,
                this.userMap,
                this.modelRes,
                this.chosenUserId
        );
        String str = String.join("\n",top10);
        
        this.mdc.insertLog("Result:\n"+String.format("# RMSE = %.3f\n",rmse));
        this.mdc.insertLog("# User ID: "+this.chosenUserId+"\n"
                + "# Top 10 Recipe Recommendation: \n"
                + str +"\n");
        this.mdc.insertLog("\nFactorization Done.");
            
    }   
    
    
 
        
        /*
        EXPERIMENT: implementing executor task
        
        int coreCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(coreCount);
        
        //method 1 - task 1
        
        
        EXPERIMENT complete
        */
    
//    class Method1 implements Callable<double[][]> {
//        public Method1(){
//            
//        }
//        
//        @Override
//        public double[][] call() throws Exception{
//            
//        }
//    }
    
    public double[][] getModel(){return this.modelRes;}
}
