/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author asus
 */
public class Recipe {

    private String id;
    private SimpleStringProperty name;
    private SimpleStringProperty userRating;
    private Ingredient ingredient;

    public Recipe(String... data) {
        this.id = data[0];
        this.name = new SimpleStringProperty(data[1]);
        this.ingredient = new Ingredient(data[2],data[3]);
        this.userRating = new SimpleStringProperty("0");
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name.get();
    }
    
    public void setName(String s){
        this.name.set(s);
    }
    
    public String getUserRating() {
        return this.userRating.get();
    }
    
    public void setUserRating(String s){
        this.userRating.set(s);
    }
    
    public String getIngredient() {
        String res = "";
        for (String i : this.ingredient.getIngredients()) {
            res += i+", ";
        }
        res = res.substring(0,res.length()-2);
        return res;
    }
    
    public Ingredient getIngredientObject() {
        return this.ingredient;
    }
    
    public int getIngredientLength(){return this.ingredient.length();}
}
