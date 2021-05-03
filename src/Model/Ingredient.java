/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author asus
 */
public class Ingredient {

    private List<String> ingredients;

    public Ingredient(String input) {
        this.ingredients = new ArrayList<String>();
        String[] ingreList = input.split(",");
        for (String x : ingreList) {
            this.ingredients.add(x);
        }
    }

    public List<String> getIngredients() {
        return this.ingredients;
    }
    
    public String getStringIngredients() {
        String res = "";
        for(String i: this.ingredients){
            res += i;
        }
        return res;
    }
}
