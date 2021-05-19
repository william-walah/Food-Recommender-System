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

    private final int LATENT_SIZE;
    private final double TRUNCATION_VAL;
    private final double LAMBDA;
    private double LEARNING_RATE;

    public FactorizationUtil(int latentSize, double lambda, double learningRate) {
        //do nothing
        this.LATENT_SIZE = latentSize;
        this.TRUNCATION_VAL = LATENT_SIZE * 5;
        this.LAMBDA = lambda;
        this.LEARNING_RATE = learningRate;
    }
    
    public void setLearningRate(double lr){this.LEARNING_RATE = lr;}

    public double objectiveFunction_m1(
            FactorMatrix user,
            FactorMatrix recipe,
            TrainMatrix trainM,
            List<Pair> trainPair,
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap
    ) {
        double sumOfErrorSquared = 0.0;
        for (Pair curr: trainPair) {
            int userIndex = userMap.get(curr.getUser());
            int recipeIndex = recipeMap.get(curr.getRecipe());
            double[] userFactor = user.getFactorByIndex(userIndex); //reference
            double[] recipeFactor = recipe.getFactorByIndex(recipeIndex); //reference
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
        double[][] userVector = user.getEntry(); //reference
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
                    int recipeIndex = map.get(filtered.get(i).getRecipe()); //map = recipeMap
                    double[] pairLatent = fm.getFactorByIndex(recipeIndex);
                    double trainValue = trainM.getEntryByIndex(index, recipeIndex);
                    double error = trainValue - (MatrixUtil.vectorMultiplication(latentVector, pairLatent) / TRUNCATION_VAL);
                    //calculate error times pair latent
                    //double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pairLatent);
                    //res = MatrixUtil.vectorCalculation(res, errorTimesPair, 1);
                    res = MatrixUtil.vectorCalculation(res, pairLatent, 1, false, error);
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
                    //double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pairLatent);
                    //res = MatrixUtil.vectorCalculation(res, errorTimesPair, 1);
                    res = MatrixUtil.vectorCalculation(res, pairLatent, 1, false, error);
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
            double[][] prediction,
            FactorMatrix user,
            FactorMatrix ingredient,
//            FactorMatrix recipeIngredientMap,
            TrainMatrix trainM,
            List<Pair> trainPair,
            List<Recipe> listOfRecipe,
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap
    ) {
        double sumOfErrorSquared = 0.0;
        for (int i = 0; i < trainPair.size(); i++) {
            Pair curr = trainPair.get(i);
            int userIndex = userMap.get(curr.getUser());
            int recipeIndex = recipeMap.get(curr.getRecipe());
            double actual = trainM.getEntryByIndex(userIndex, recipeIndex);
//            double[] userFactor = user.getFactorByIndex(userIndex); //length f
//            double[][] ingredientsFactor = ingredient.getEntry(); //dimension of o(ingredient) x f
//            double[] recipeMask = recipeIngredientMap.getFactorByIndex(recipeIndex); //length o
//            double predicted = MatrixUtil.vectorMultiplication(
//                    MatrixUtil.vectorMultiplyMatrix(userFactor, ingredientsFactor),
//                    recipeMask
//            );
            double predicted = prediction[userIndex][recipeIndex];
            predicted = predicted / (TRUNCATION_VAL*listOfRecipe.get(recipeIndex).getIngredientLength());
//            System.out.println(actual+","+predicted);
            sumOfErrorSquared += Math.pow((actual - predicted), 2);
        }
        double penalty = LAMBDA * (user.calculateVectorLength() + ingredient.frobeniusNorm());
        return sumOfErrorSquared + penalty;
    }

    public void alternatingGradientDescent_m2( //method 2
            double[][] prediction,
            FactorMatrix user,
            FactorMatrix ingredient,
            FactorMatrix recipeIngredientMap,
            List<Pair> trainPair,
            List<Recipe> recipes,
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
                            prediction,
                            userVector[i],
                            i,
                            ingredient,
                            recipeIngredientMap,
                            trainPair,
                            recipes,
                            recipeMap,
                            userMap_r,
                            tm), //learning value
                    1); //addition
            for (int j = 0; j < newLatent.length; j++) {
                if (newLatent[j] > 5.0) {
                    newLatent[j] = 5.0;
                } else if (newLatent[j] < 0.0) {
                    newLatent[j] = 0.0;
                }
            }
            userVector[i] = newLatent;
        }
        //update the ingredients matrix factor value
        double[][] lvIngredientMatrix = calcLearningValue_m2_ingredient(
            prediction,
            ingredient.getEntry(),
            user,
            recipeIngredientMap,
            trainPair,
            recipes,
            userMap, 
            recipeMap,
            tm
        );
        for (int i = 0; i < lvIngredientMatrix.length; i++) {
            for (int j = 0; j < lvIngredientMatrix[i].length; j++) {
                if (lvIngredientMatrix[i][j] > 5.0) {
                    lvIngredientMatrix[i][j] = 5.0;
                } else if (lvIngredientMatrix[i][j] < 0.0) {
                    lvIngredientMatrix[i][j] = 0.0;
                }
            }
        }
        ingredient.add(lvIngredientMatrix);
    }

    public double[] calcLearningValue_m2_user( //for user vector
            double[][] prediction,
            double[] latentVector,
            int index, //userIndex
            FactorMatrix ingredient, //ingredient factor latent
            FactorMatrix recipeIngredientMap, //recipe x ingredients (1/0)
            List<Pair> observable,
            List<Recipe> listOfRecipe,
            HashMap<String, Integer> recipeMap, //recipeMap
            HashMap<Integer, String> reverseMap, //user reverse map
            TrainMatrix trainM
    ) {
        String targetId = reverseMap.get(index);
        double[] res = new double[latentVector.length];
        double[] lambdaTarget = MatrixUtil.scalarMultiplication(LAMBDA, latentVector);
//        double[][] inrgedientMatrix = ingredient.getEntry();
        //calculate pair... why not do inside loop? too slow
        double[][] pair = recipeIngredientMap.mulitply(ingredient.getEntry());
        //List<Pair> filtered = observable.stream().filter(p -> p.getUser().equals(targetId)).collect(Collectors.toList());
        for (Pair curr: observable) {
            if(curr.getUser().equals(targetId)){
                int recipeIndex = recipeMap.get(curr.getRecipe()); //map = recipeMap
                double trainValue = trainM.getEntryByIndex(index, recipeIndex); // nilai training
//                double[] recipeIngredientVector = recipeIngredientMap.getFactorByIndex(recipeIndex); // vector berisi nilai 1 / 0 bila makanan memiliki bahan di index-i
//                double[] pair = MatrixUtil.vectorMultiplyMatrix(recipeIngredientVector, inrgedientMatrix); // vector hasil perkalian matrix bahan makanan dengan vector bahan resep makanan
                double predicted = prediction[index][recipeIndex];
                double error = trainValue - (predicted / (TRUNCATION_VAL* listOfRecipe.get(recipeIndex).getIngredientLength())); // nilai error / prediksi
                /* 
                above calculation (errorTimesPair) too slow, can be integrated like below
                //double[] errorTimesPair = MatrixUtil.scalarMultiplication(error, pair[recipeIndex]);//calculate error times pair
                //res = MatrixUtil.vectorCalculation(res, errorTimesPair, 1);
                */
                res = MatrixUtil.vectorCalculation(res,pair[recipeIndex],1,false,error);
            }
        }
        //res = MatrixUtil.vectorCalculation(res, lambdaTarget, 0);
        //calculate result * learning rate
        //res = MatrixUtil.scalarMultiplication(LEARNING_RATE, res);
        res = MatrixUtil.vectorCalculation(res, lambdaTarget, 0, true, LEARNING_RATE);
        return res;
    }
    
    public double[][] calcLearningValue_m2_ingredient( //for ingredient matrix
            double[][] prediction,
            double[][] matrix,
            FactorMatrix user, 
            FactorMatrix recipeIngredientMap, //recipe x ingredients (1/0)
            List<Pair> observable,
            List<Recipe> listOfRecipe,
            HashMap<String, Integer> userMap, 
            HashMap<String, Integer> recipeMap,
            TrainMatrix trainM
    ) {
        double[][] res = new double[matrix.length][matrix[0].length];
        double[][] lambdaValue = MatrixUtil.scalarMultiplication(LAMBDA, matrix);
        for (Pair curr: observable) {
            int userIndex = userMap.get(curr.getUser());
            int recipeIndex = recipeMap.get(curr.getRecipe());
            double actual = trainM.getEntryByIndex(userIndex, recipeIndex);
            double[] userVector = user.getFactorByIndex(userIndex);
            double[] recipeMask = recipeIngredientMap.getFactorByIndex(recipeIndex);
//            double predicted =  
//                    MatrixUtil.vectorMultiplication(
//                            MatrixUtil.vectorMultiplyMatrix(userVector, matrix),
//                            recipeMask
//                    );
            double predicted = prediction[userIndex][recipeIndex];
            double error = (actual-predicted) / (TRUNCATION_VAL* listOfRecipe.get(recipeIndex).getIngredientLength());
            double[][] pair = MatrixUtil.vectorMultiplicationToMatrix(recipeMask, userVector);
//            res = MatrixUtil.matrixCalculation(
//                    res,
//                    MatrixUtil.scalarMultiplication(
//                            error,
//                            pair
//                            ),
//                    1
//            ); too slow
            res = MatrixUtil.matrixCalculation(res,pair,1,false,error);
        }
        res = MatrixUtil.matrixCalculation(res, lambdaValue, 0);
        return res;
    }

    public double rmse(
            List<Pair> testPair,
            List<Recipe> listOfRecipe,
            HashMap<String, Integer> userMap,
            HashMap<String, Integer> recipeMap,
            double[][] modelRes,
            double[][] matrixData,
            int method
    ) { 
        double squaredError = 0.0;
        for (int i = 0; i < testPair.size(); i++) {
            Pair curr = testPair.get(i);
            int userIndex = userMap.get(curr.getUser());
            int recipeIndex = recipeMap.get(curr.getRecipe());
            double predicted = 0.0;
            if(method<1){ //method_1
                predicted = modelRes[userIndex][recipeIndex] / TRUNCATION_VAL;
            } else { //method_2
                predicted = modelRes[userIndex][recipeIndex] / (TRUNCATION_VAL* listOfRecipe.get(recipeIndex).getIngredientLength());
            }
            double actual = matrixData[userIndex][recipeIndex];
            if (actual < 1) {
                throw new RuntimeException("Missing value in test matrix");
            }
            squaredError += Math.pow((predicted - actual), 2);
        }
        return Math.sqrt(squaredError / (double) testPair.size());
    }

    public int getLatentSize() {
        return LATENT_SIZE;
    }
}
