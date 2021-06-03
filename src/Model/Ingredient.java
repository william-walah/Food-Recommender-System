/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author asus
 */
public class Ingredient {

    private List<String> ingredients;
    private List<String> ingredientsId;
    private HashMap<String, String> ingredientMap;
    private int uniqueLength;

    public Ingredient(String input, String id) {
        this.uniqueLength = 0;
        this.ingredients = new ArrayList<String>();
        this.ingredientsId = new ArrayList<String>();
        this.ingredientMap = new HashMap<String, String>();
        String[] ingreList = input.split("_");
        String[] ingreIdList = id.split(",");
        if(ingreList.length != ingreIdList.length) throw new RuntimeException("Error Missmatch ID length and Ingredient Length when parsing data");
        for (int i = 0; i < ingreIdList.length; i++) {
            this.ingredients.add(ingreList[i]);
            if(!ingredientsId.contains(ingreIdList[i])) this.uniqueLength++;
            this.ingredientsId.add(ingreIdList[i]);
            this.ingredientMap.put(ingreList[i], ingreIdList[i]);
        }
    }

    public List<String> getIngredients() {
        return this.ingredients;
    }
    
    public List<String> getIngredientIds() {
        return this.ingredientsId;
    }
    
    public HashMap<String,String> getIngredientMap() {
        return this.ingredientMap;
    }
    
    public int length(){return this.uniqueLength;}
}
