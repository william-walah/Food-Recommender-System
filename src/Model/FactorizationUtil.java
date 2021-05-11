/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author asus
 */
public class FactorizationUtil {

    private final int LATENT_SIZE = 2;
    private final double TRUNCATION_VAL = LATENT_SIZE * 5;
    private final double TRUNCATION_VAL_2;
    private final double LAMBDA = 1;
    private final double LEARNING_RATE = 0.0001;

    public FactorizationUtil(int ingredientSize) {
        //do nothing
        TRUNCATION_VAL_2 = ingredientSize;
    }

    public double objectiveFunction_m1(
            FactorMatrix user,
            FactorMatrix recipe,
            TrainMatrix trainM,
            List<Pair> trainPair,
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap
    ) {
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
            predicted = predicted / TRUNCATION_VAL;
            //System.out.println(String.format("%.3f_p %.3f_a",predicted,trainM.getEntryByIndex(userIndex, recipeIndex)));
            sumOfErrorSquared += Math.pow((trainM.getEntryByIndex(userIndex, recipeIndex) - predicted), 2);
        }

//        System.out.println(String.format("%.3f",sumOfErrorSquared));
        double penalty = LAMBDA * (user.calculateVectorLength() + recipe.calculateVectorLength());
//        System.out.println(String.format("%.3f",penalty));
        return sumOfErrorSquared + penalty;
    }

    public void alternatingGradientDescent_m1( //method 1
            FactorMatrix user,
            FactorMatrix recipe,
            List<Pair> trainPair,
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap,
            HashMap<Integer, String> userMap_r,
            HashMap<Integer, String> recipeMap_r,
            TrainMatrix tm
    ) {
        //update the user matrix factor value
        double[][] userVector = user.getEntry();
        for (int i = 0; i < userVector.length; i++) {
            double[] newLatent = MatrixUtil.vectorCalculation(
                    userVector[i], //previous value
                    calculateLearningValue_m1(
                            userVector[i],
                            i,
                            recipe,
                            trainPair,
                            recipeMap,
                            userMap_r,
                            FactorType.USER,
                            tm), //learning value
                    1); //addition
            //um... ubah value kalau lebih dari 5 jadi 5, kurang dari 0 jadi 0?
            for (int j = 0; j < newLatent.length; j++) {
                if (newLatent[j] > 5) {
                    newLatent[j] = 5.0;
                } else if (newLatent[j] < 0) {
                    newLatent[j] = 0.0;
                }
            }
            userVector[i] = newLatent;
        }
        //update the recipe matrix factor value
        double[][] recipeVetor = recipe.getEntry();
        for (int i = 0; i < recipeVetor.length; i++) {
            double[] newLatent = MatrixUtil.vectorCalculation(
                    recipeVetor[i], //previous value
                    calculateLearningValue_m1(
                            recipeVetor[i],
                            i,
                            user,
                            trainPair,
                            userMap,
                            recipeMap_r,
                            FactorType.RECIPES,
                            tm), //learning value
                    1); //addition
            for (int j = 0; j < newLatent.length; j++) {
                if (newLatent[j] > 5) {
                    newLatent[j] = 5.0;
                } else if (newLatent[j] < 0) {
                    newLatent[j] = 0.0;
                }
            }
            recipeVetor[i] = newLatent;
        }
    }

    public double[] calculateLearningValue_m1(
            double[] latentVector,
            int index,
            FactorMatrix fm,
            List<Pair> observable,
            HashMap<String, Integer> map, //either userMap, recipeMap, or ingredientsMap
            HashMap<Integer, String> reverseMap,
            FactorType type,
            TrainMatrix trainM
    ) {

        String targetId = reverseMap.get(index);
        double[] res = new double[latentVector.length];
        double[] lambdaTarget = null;
        List<Pair> filtered = null;

        switch (type) {
            case USER:
                filtered = observable.stream().filter(p -> p.getUser().equals(targetId)).collect(Collectors.toList());
                //calculate lambda times targeted latent vector
                lambdaTarget = MatrixUtil.scalarMultiplication(LAMBDA, latentVector);
                for (int i = 0; i < filtered.size(); i++) {
                    int recipe_index = map.get(filtered.get(i).getRecipe()); //map = recipeMap
                    double[] pairLatent = fm.getFactorByIndex(recipe_index);
                    double trainValue = trainM.getEntryByIndex(index, recipe_index);
                    double error = trainValue - (MatrixUtil.vectorMultiplication(latentVector, pairLatent) / TRUNCATION_VAL);
                    //calculate error times pair latent
                    double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pairLatent);
                    res = MatrixUtil.vectorCalculation(res, errorTimesPair, 1);
                }
                res = MatrixUtil.vectorCalculation(res, lambdaTarget, 0);
                break;
            case RECIPES:
                filtered = observable.stream().filter(p -> p.getRecipe().equals(targetId)).collect(Collectors.toList());
                //calculate lambda times targeted latent vector
                lambdaTarget = MatrixUtil.scalarMultiplication(LAMBDA, latentVector);
                for (int i = 0; i < filtered.size(); i++) {
                    int user_index = map.get(filtered.get(i).getUser()); //map = userMap
                    double[] pairLatent = fm.getFactorByIndex(user_index);
                    double trainValue = trainM.getEntryByIndex(user_index, index);
                    double error = trainValue - (MatrixUtil.vectorMultiplication(latentVector, pairLatent) / TRUNCATION_VAL);
                    //calculate error times pair latent
                    double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pairLatent);
                    res = MatrixUtil.vectorCalculation(res, errorTimesPair, 1);
                }
                res = MatrixUtil.vectorCalculation(res, lambdaTarget, 0);
                break;
            default: //do nothing
                break;
        }
        //calculate result * learning rate
        res = MatrixUtil.scalarMultiplication(LEARNING_RATE, res);
        return res;
    }

    public double objectiveFunction_m2(
            FactorMatrix user,
            FactorMatrix ingredient,
            FactorMatrix recipeIngredientMap,
            TrainMatrix trainM,
            List<Pair> trainPair,
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap
    ) {
        double sumOfErrorSquared = 0.0;
        for (int i = 0; i < trainPair.size(); i++) {
            Pair curr = trainPair.get(i);
            int userIndex = userMap.get(curr.getUser());
            int recipeIndex = recipeMap.get(curr.getRecipe());
            double actual = trainM.getEntryByIndex(userIndex, recipeIndex);
            double[] userFactor = user.getFactorByIndex(userIndex); //length f
            double[][] ingredientsFactor = ingredient.getEntry(); //dimension of o(ingredient) x f
            double[] recipeMask = recipeIngredientMap.getFactorByIndex(recipeIndex); //length o
            double predicted = MatrixUtil.vectorMultiplication(
                    MatrixUtil.vectorMultiplyMatrix(userFactor, ingredientsFactor),
                    recipeMask
            );
            predicted = predicted / TRUNCATION_VAL_2;
            sumOfErrorSquared += Math.pow((actual - predicted), 2);
        }
        double penalty = LAMBDA * (user.calculateVectorLength() + ingredient.frobeniusNorm());
        return sumOfErrorSquared + penalty;
    }

    public void alternatingGradientDescent_m2( //method 2
            FactorMatrix user,
            FactorMatrix ingredient,
            FactorMatrix recipeIngredientMap,
            List<Pair> trainPair,
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap,
            HashMap<Integer, String> userMap_r,
            HashMap<Integer, String> recipeMap_r,
            TrainMatrix tm
    ) {
        //update the user matrix factor value
        double[][] userVector = user.getEntry();
        for (int i = 0; i < userVector.length; i++) {
            double[] newLatent = MatrixUtil.vectorCalculation(
                    userVector[i], //previous value
                    calcLearningValue_m2_user(
                            userVector[i],
                            i,
                            ingredient,
                            recipeIngredientMap,
                            trainPair,
                            recipeMap,
                            userMap_r,
                            tm), //learning value
                    1); //addition
            for (int j = 0; j < newLatent.length; j++) {
                if (newLatent[j] > 5) {
                    newLatent[j] = 5.0;
                } else if (newLatent[j] < 0) {
                    newLatent[j] = 0.0;
                }
            }
            userVector[i] = newLatent;
        }
        //update the ingredients matrix factor value
        double[][] lvIngredientMatrix = calcLearningValue_m2_ingredient(
          ingredient.getEntry(),
                user,
                recipeIngredientMap,
                trainPair,
                userMap, 
                recipeMap,
                tm
        );
        ingredient.add(lvIngredientMatrix);
    }

    public double[] calcLearningValue_m2_user( //for user vector
            double[] latentVector,
            int index,
            FactorMatrix fm, //ingredient factor latent
            FactorMatrix recipeIngredientMap, //recipe x ingredients (1/0)
            List<Pair> observable,
            HashMap<String, Integer> recipeMap, //recipeMap
            HashMap<Integer, String> reverseMap, //user reverse map
            TrainMatrix trainM
    ) {
        String targetId = reverseMap.get(index);
        double[] res = new double[latentVector.length];
        double[] lambdaTarget = MatrixUtil.scalarMultiplication(LAMBDA, latentVector);
        double[][] inrgedientMatrix = fm.getEntry();
        List<Pair> filtered = observable.stream().filter(p -> p.getUser().equals(targetId)).collect(Collectors.toList());
        for (int i = 0; i < filtered.size(); i++) {
            int recipe_index = recipeMap.get(filtered.get(i).getRecipe()); //map = recipeMap
            double trainValue = trainM.getEntryByIndex(index, recipe_index); // nilai training
            double[] recipeIngredientVector = recipeIngredientMap.getFactorByIndex(recipe_index); // vector berisi nilai 1 / 0 bila makanan memiliki bahan di index-i
            double[] pair = MatrixUtil.vectorMultiplyMatrix(recipeIngredientVector, inrgedientMatrix); // vector hasil perkalian matrix bahan makanan dengan vector bahan resep makanan
            double error = trainValue - (MatrixUtil.vectorMultiplication(latentVector, pair) / TRUNCATION_VAL_2); // nilai error / prediksi
            double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pair);//calculate error times pair
            res = MatrixUtil.vectorCalculation(res, errorTimesPair, 1);
        }
        res = MatrixUtil.vectorCalculation(res, lambdaTarget, 0);
        //calculate result * learning rate
        res = MatrixUtil.scalarMultiplication(LEARNING_RATE, res);
        return res;
    }
    
    public double[][] calcLearningValue_m2_ingredient( //for ingredient matrix
            double[][] matrix,
            FactorMatrix user, 
            FactorMatrix recipeIngredientMap, //recipe x ingredients (1/0)
            List<Pair> observable,
            HashMap<String, Integer> userMap, 
            HashMap<String, Integer> recipeMap,
            TrainMatrix trainM
    ) {
        double[][] res = new double[matrix.length][matrix[0].length];
        double[][] lambdaValue = MatrixUtil.scalarMultiplication(LAMBDA, matrix);
        for (Pair curr: observable) {
            int userIdx = userMap.get(curr.getUser());
            int recipeIdx = recipeMap.get(curr.getRecipe());
            double actual = trainM.getEntryByIndex(userIdx, recipeIdx);
            double[] userVector = user.getFactorByIndex(userIdx);
            double[] recipeMask = recipeIngredientMap.getFactorByIndex(recipeIdx);
            double predicted = 
                    MatrixUtil.vectorMultiplication(
                            MatrixUtil.vectorMultiplyMatrix(userVector, matrix),
                            recipeMask
                    );
            double error = (actual-predicted) / TRUNCATION_VAL_2;
            res = MatrixUtil.matrixCalculation(
                    res,
                    MatrixUtil.scalarMultiplication(
                            error,
                            MatrixUtil.vectorMultiplicationToMatrix(userVector, recipeMask)
                            ),
                    1
            );
        }
        res = MatrixUtil.matrixCalculation(res, lambdaValue, 0);
        return res;
    }

    public double rmse(
            List<Pair> testPair,
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap,
            double[][] modelRes,
            TestMatrix testM,
            int method
    ) { //double[][] prediction and TestMatrix
        System.out.println("bee boop dipslaying rmse process");

        double squaredError = 0.0;
        for (int i = 0; i < testPair.size(); i++) {
            Pair curr = testPair.get(i);
            int userIndex = userMap.get(curr.getUser());
            int recipeIndex = recipeMap.get(curr.getRecipe());
            double predicted = 0.0;
            if(method<1){ //method_1
                predicted = modelRes[userIndex][recipeIndex] / TRUNCATION_VAL;
            } else { //method_2
                predicted = modelRes[userIndex][recipeIndex] / TRUNCATION_VAL_2;
            }
            double actual = testM.getEntryByIndex(userIndex, recipeIndex);
            System.out.println(String.format("%d. Predicted = %.2f, Actual = %.2f", (i + 1), predicted, actual));
            if (actual < 1) {
                throw new RuntimeException("Missing value in test matrix");
            }
            squaredError += Math.pow((predicted - actual), 2);
        }
        return Math.sqrt(squaredError / (double) testPair.size());
    }

    public String[] topTenRecipe(
            List<Recipe> recipes,
            HashMap<String, Integer> userMap,
            double[][] modelRes,
            String chosenUserId
    ) {
        int userIndex = userMap.get(chosenUserId);
        List<RecipePredicted> l = new ArrayList<RecipePredicted>();
        for (int i = 0; i < modelRes[userIndex].length; i++) {
            l.add(new RecipePredicted(modelRes[userIndex][i], i));
        }
        Collections.sort(l);
        String[] res = new String[10];
        for (int i = 0; i < 10; i++) {
            RecipePredicted curr = l.get(i);
            String temp = "# " + (i + 1) + ". " + recipes.get(curr.getIndex()).getName()
                    + " (" + String.format("%.2f", curr.getValue() / TRUNCATION_VAL) + ")";

            res[i] = temp;
        }
        return res;
    }

    public int getLatentSize() {
        return LATENT_SIZE;
    }
}
