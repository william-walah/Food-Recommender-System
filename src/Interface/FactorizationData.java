/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Model.Dataset;
import Model.FactorMatrix;
import Model.Ingredient;
import Model.Recipe;
import Model.Recipe;
import Model.Recipe;
import Model.TestMatrix;
import Model.TestMatrix;
import Model.TestMatrix;
import Model.TrainMatrix;
import Model.TrainMatrix;
import Model.TrainMatrix;
import Model.User;
import Model.User;
import Model.User;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author asus
 */
public interface FactorizationData {

    public List<Recipe> getRecipes();

    public List<User> getUsers();
    
    public List<String> getIngredient();
    
    public TrainMatrix getTrainMatrix();
    
    public TestMatrix getTestMatrix();
    
    public HashMap<String, Integer> getUserMap();
    
    public HashMap<String, Integer> getRecipeMap();
    
    public HashMap<String, Integer> getIngredientMap();
    
    public HashMap<Integer, String> getUserMapReversed();
    
    public HashMap<Integer, String> getRecipeMapReversed();
    
    public HashMap<Integer, String> getIngredientMapReversed();
}
