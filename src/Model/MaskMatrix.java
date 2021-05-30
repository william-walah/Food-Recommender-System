/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author asus
 */
public class MaskMatrix extends Matrix{
    public MaskMatrix(int row, int col, FactorType t, 
            List<Recipe> recipe,
            HashMap<String, Integer> recipeIdx,
            HashMap<String, Integer> ingredientIdx){
        super(row,col);
        
        for (Recipe curr: recipe) {
            int rowIdx = recipeIdx.get(curr.getId());
            List<String> usingIds = curr.getIngredientObject().getIngredientIds();
            for(String id: usingIds){
                int colIdx = ingredientIdx.get(id);
                entry[rowIdx][colIdx] = 1.0;
            }
        }
        
        for (int i = 0; i < entry.length; i++) {
            for (int j = 0; j < entry[i].length; j++) {
                System.out.print(entry[i][j]+",");
            }
            System.out.println("");
        }
        System.out.println("");
    }
    
    
}
