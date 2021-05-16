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
import java.io.InputStream;
import java.io.InputStreamReader;
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
    //private String chosenUserId;
    private MainDocumentController mdc;
    private TrainMatrix trainM;
    private TestMatrix testM;
    private Dataset d;
    private FactorizationUtil utils;
//    private double[][] modelRes;
    private int MAX_LOOP;
    private final double MIN_OBJECTIVE_VAL = 10.0;

    //
    private List<Pair> trainPair;
    private HashMap<String, Integer> userMap;     //memetakan ID user kepada index matrix user & list user
    private HashMap<String, Integer> recipeMap;   //memetakan ID resep kepada index matrix resep & list resep
    private HashMap<String, Integer> ingredientMap;
    private HashMap<Integer, String> userMap_r;   //memetakan index matrix / list user dengan ID user
    private HashMap<Integer, String> recipeMap_r; //memetakan index matriks / list resep dengan ID resep
    private HashMap<Integer, String> ingredientMap_r;
    private List<double[][]> result;
    private int learningType; //fixed or iteratively change to 1/t

    public Factorization(MainDocumentController c) {
        this.recipes = new ArrayList<Recipe>();
        this.result = new ArrayList<double[][]>();
        this.users = new ArrayList<User>();
        this.ingredients = new ArrayList<String>();
        this.ingredientMap = new HashMap<String, Integer>();
        this.ingredientMap_r = new HashMap<Integer, String>();
        this.mdc = c;
        this.trainPair = null;
        this.userMap = null;
        this.userMap_r = null;
        this.recipeMap = null;
        this.recipeMap_r = null;
    }

    public boolean readRecipesData() {
        boolean success = false;
        try {
            //this method only works in java IDE
            //File file = new File("./data/dataset_recipes_ingredient_list_readable_java.csv");
            //FileReader fr = new FileReader(fr);
            //BufferedReader br = new BufferedReader(fr);
            //to be usable in jar, look below. Thanks: https://stackoverflow.com/questions/20389255/reading-a-resource-file-from-within-jar
            InputStream in = getClass().getResourceAsStream("/data/dataset_recipes_ingredient_list_readable_java.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            String[] tempArr;
            Set<String> ingredientsUniqueId = new HashSet<String>();
            while ((line = br.readLine()) != null) {
                tempArr = line.split("[|]+");
                this.recipes.add(new Recipe(tempArr));
                ingredientsUniqueId.addAll(Arrays.asList(tempArr[3].split(",")));
            }
            br.close();
            int index = 0;
            for (String uniqueId : ingredientsUniqueId) {
                this.ingredients.add(uniqueId);
                this.ingredientMap.put(uniqueId, index);
                this.ingredientMap_r.put(index, uniqueId);
                index++;
            }
            success = true;
        } catch (Exception ioe) {
            ioe.printStackTrace();
            System.exit(1);
        } finally {
            return success;
        }
    }

    public boolean readUsersData() {
        boolean success = false;
        try {
            //File file = new File("./data/dataset_userId_list.csv");
            //FileReader fr = new FileReader(file);
            //BufferedReader br = new BufferedReader(fr);
            InputStream in = getClass().getResourceAsStream("/data/dataset_userId_list.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null) {
                this.users.add(new User(line));
            }
            br.close();
            success = true;
        } catch (Exception ioe) {
            ioe.printStackTrace();
        } finally {
            return success;
        }
    }

    public List<Recipe> getRecipes() {
        return this.recipes;
    }

    public List<User> getUsers() {
        return this.users;
    }

//    public void setUserId(String s) {
//        chosenUserId = s;
//    }
    
    public void setParameter(String... data){
        this.utils = new FactorizationUtil(
           Integer.parseInt(data[0]),
           Double.parseDouble(data[2]),
           Double.parseDouble(data[3])
        );
        this.MAX_LOOP = Integer.parseInt(data[1]);
        this.learningType = data[4].equals("Fixed") ? 0 : 1;
    }

    //creating the first method of factorization (w_o ing   redients matrix)
    @Override
    public void run() {
        this.mdc.insertLog("Starting factorization process \n#1 Reading Dataset\n");
        //first time Dataset d initialization
        this.d = new Dataset(this.recipes, this.users);
        //1. Read Dataset
        boolean succesReadDataset = this.d.read();
        if (!succesReadDataset) {
            this.mdc.insertLog("Error: Failed to read dataset: dataset_readable_java.csv\n");
            return;
        } else {
            this.mdc.insertLog("Success loading the dataset\n");
        }

        //set attribute
        this.trainPair = this.d.getTrainPair();
        this.userMap = this.d.getUserMap();
        this.recipeMap = this.d.getRecipeMap();
        this.userMap_r = this.d.getReverseUserMap();
        this.recipeMap_r = this.d.getReverseRecipeMap();
        //FactorizationUtil utils = new FactorizationUtil();

        this.mdc.insertLog("#2 Splitting Dataset\n");
        //2. Split Dataset
        boolean stg2 = false;
        try {
            List<Object> split_mat = this.d.split();
            this.trainM = (TrainMatrix) split_mat.remove(0);
            this.testM = (TestMatrix) split_mat.remove(0);
            stg2 = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stg2) {
                this.mdc.insertLog("Success splitting dataset\n");
            } else {
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
        try {
            // user factor = m x f
            userFactor = new FactorMatrix(this.trainM.getRowLength(), utils.getLatentSize(), FactorType.USER);
            // recipe factor = n x f
            recipeFactor = new FactorMatrix(this.trainM.getColLength(), utils.getLatentSize(), FactorType.RECIPES);
            // ingredient factor = o x f
            ingredientFactor = new FactorMatrix(this.ingredients.size(), utils.getLatentSize(), FactorType.INGREDIENTS);
            // recipe x ingredient map matrix = n x o (mask matrix 1/0)
            recipeIngredientsMap = new FactorMatrix(this.trainM.getColLength(),
                    this.ingredients.size(),
                    FactorType.RECIPE_ING_MAP,
                    this.recipes,
                    this.recipeMap,
                    this.ingredientMap);
            userFactor_2 = new FactorMatrix(userFactor);
            stg3 = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stg3) {
                this.mdc.insertLog("Success initializing matrix factor dataset\n");
            } else {
                this.mdc.insertLog("Error: Failed to initialize matrix factor\n");
                return;
            }
        }

        this.mdc.insertLog("#4 Entering Optimization Loop process\n"
                + "#######\n"
                + "# Process No. 1 Detail:\n"
                + "# Max Number of iteration: " + MAX_LOOP + "\n"
                + "# Method: Factorization with two Matrix Factor-User & Recipes\n"
                + "#######\n"
        );
        //4. entering loop  
        //loop a thousand time or break if objective value less than MIN_OBJECTIVE_VAL
        boolean stg4_1 = false;
        long start = System.currentTimeMillis();     
        double objFuncPrev_1 = 0.0;       
        try{
            int loop = MAX_LOOP;
            while(loop-->0){
                if(loop%250==0) System.out.println("loop: "+(MAX_LOOP-loop));
                double objectiveValue = utils.objectiveFunction_m1(
                        userFactor,
                        recipeFactor,
                        this.trainM,
                        this.trainPair,
                        this.userMap,
                        this.recipeMap
                );
                //System.out.println(objectiveValue);
                if(objectiveValue <= MIN_OBJECTIVE_VAL) break;
                else if (Math.abs(objFuncPrev_1 - objectiveValue) <= 0.00001) {
                    break;
                }
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
            stg4_1 = true;
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        } finally {
            if(stg4_1) this.mdc.insertLog("1st Optimization process done\n");
            else{ 
                this.mdc.insertLog("Error: Failure occured in optimization process\n");
                return;
            }
        }
        long elapsedTime_1 = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis(); 
        this.mdc.insertLog("#######\n"
                + "# Process No. 2 Detail:\n"
                + "# Max Number of iteration: " + MAX_LOOP + "\n"
                + "# Method: Factorization with two Matrix Factor-User & Ingredients\n"
                + "#######\n"
        );
        boolean stg4_2 = false;
        double objFuncPrev_2 = 0.0;
        try {
            int loop = MAX_LOOP;
            while (loop-- > 0) {
                //if(loop%250==0) System.out.println("loop: "+(1000-loop));
                System.out.println("loop: " + (MAX_LOOP - loop));
                double[][] currPrediction = MatrixUtil.multiplyWithTransposing(
                        userFactor_2.getEntry(),
                        recipeIngredientsMap.mulitply(
                                ingredientFactor.getEntry()
                        ),
                        false);
                double objectiveValue = utils.objectiveFunction_m2(
                        currPrediction,
                        userFactor_2,
                        ingredientFactor,
                        //recipeIngredientsMap,
                        this.trainM,
                        this.trainPair,
                        this.recipes,
                        this.userMap,
                        this.recipeMap
                );
                if (objectiveValue <= MIN_OBJECTIVE_VAL) {
                    break;
                } else if (Math.abs(objFuncPrev_2 - objectiveValue) <= 0.00001) {
                    break;
                } else if (Double.isNaN(objectiveValue)) {
                    throw new RuntimeException("Objective Function value is NaN");
                } else {
                    //do A-GD
                    objFuncPrev_2 = objectiveValue;
                    utils.alternatingGradientDescent_m2(
                            currPrediction,
                            userFactor_2,
                            ingredientFactor,
                            recipeIngredientsMap,
                            this.trainPair,
                            this.recipes,
                            this.userMap,
                            this.recipeMap,
                            this.userMap_r,
                            this.recipeMap_r,
                            this.trainM
                    );
                }
            }
            stg4_2 = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (stg4_2) {
                this.mdc.insertLog("2nd Optimization process done\n");
            } else {
                this.mdc.insertLog("Error: Failure occured in optimization process\n");
                return;
            }
        }
        long elapsedTime_2 = System.currentTimeMillis() - start;
        //check model result
//        this.modelRes = userFactor.mulitply(recipeFactor.transpose());
//        List<String[]> matrix_csv = new ArrayList<String[]>();
//        for (int i = 0; i < modelRes.length; i++) {
//            matrix_csv.add(new String[modelRes[i].length]);
//            String[] curr = matrix_csv.get(0);
//            for (int j = 0; j < curr.length; j++) {
//                curr[j] = String.format("%.3f",modelRes[i][j]);
//            }
//        }
//        try{
//            //File f = new File(getClass().getResource("/"))
//            CSVWriter writer = new CSVWriter(new FileWriter("C:\\Users\\asus\\Desktop\\model.csv", true));
//            for (String[] s: matrix_csv) {
//                writer.writeNext(s);
//            }
//            writer.close();
//            this.mdc.insertLog("Model saved in csv\n");
//        } catch(Exception e) {
//            e.printStackTrace();
//        } 
        double[][] model1 = userFactor.mulitply(recipeFactor.transpose());
        double[][] model2 = MatrixUtil.multiplyWithTransposing(
                userFactor_2.getEntry(),
                recipeIngredientsMap.mulitply(
                        ingredientFactor.getEntry()
                ),
                false);

        this.result.add(model1);
        this.result.add(model2);
        
        double rmse_1 = utils.rmse(
                this.d.getTestPair(),
                this.recipes,
                this.userMap,
                this.recipeMap,
                model1,
                this.testM,
                0
        );
        double rmse_2 = utils.rmse(
                this.d.getTestPair(),
                this.recipes,
                this.userMap,
                this.recipeMap,
                model2,
                this.testM,
                1
        );
        
//        String[] top10_1 = utils.topTenRecipe(
//                this.recipes,
//                this.userMap,
//                model1,
//                this.chosenUserId,
//                0
//        );
//        String[] top10_2 = utils.topTenRecipe(
//                this.recipes,
//                this.userMap,
//                model2,
//                this.chosenUserId,
//                1
//        );
//
//        String str_1 = String.join("\n",top10_1);
//        String str_2 = String.join("\n", top10_2);

        this.mdc.insertLog("Result:\n"
                + "############################\n"
                + "# FIRST OPTIMIZATION METHOD\n"
                + String.format("# RMSE = %.3f\n",rmse_1)
                + String.format("# Elapsed Time: %.3f\n", elapsedTime_1/1000F)
                //+ "# User ID: "+this.chosenUserId+"\n"
                //+ "# Top 10 Recipe Recommendation: \n"
                //+ str_1 +"\n"   
                + "############################\n"
                + "############################\n"
                + "# SECOND OPTIMIZATION METHOD\n"
                + String.format("# RMSE = %.3f\n", rmse_2)
                + String.format("# Elapsed Time: %.3f", elapsedTime_2/1000F)
                //+ "# User ID: " + this.chosenUserId + "\n"
                //+ "# Top 10 Recipe Recommendation: \n"
                //+ str_2 + "\n"
                + "############################\n");
        this.mdc.insertLog("\nFactorization Done.");    
        this.mdc.afterFactorization();
    }
    
    public double[][] getModel(int model) {
        //int model = 0 | 1
        return this.result.get(model);
    }
    
    public List<RecipePredicted> getUserPrediction(String userId){
        int userIndex = this.userMap.get(userId);
        List<RecipePredicted> result = new ArrayList<RecipePredicted>();
        try{
            System.out.println(this.result.size());
            double[][] firstMethodResult = this.result.get(0);
            double[][] secondMethodResult = this.result.get(1);
            for (Recipe curr: this.recipes) {
                int recipeIndex = this.recipeMap.get(curr.getId());
                result.add(new RecipePredicted(
                        curr.getName(),
                        String.format("%.2f",this.d.getEntryByIndex(userIndex, recipeIndex)),
                        String.format("%.2f",firstMethodResult[userIndex][recipeIndex]/(utils.getLatentSize()*5)),
                        String.format("%.2f",secondMethodResult[userIndex][recipeIndex]/(utils.getLatentSize()*5*curr.getIngredientLength()))
                ));
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            return result;   
        }
    }
}
