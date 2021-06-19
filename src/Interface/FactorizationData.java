package Interface;

import Model.Recipe;
import Model.TestMatrix;
import Model.TrainMatrix;
import Model.User;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author William Walah - 2017730054
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
