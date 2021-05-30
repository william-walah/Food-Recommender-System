/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 *
 * @author asus
 */
public class FactorMatrix extends Matrix{
    private FactorType type;
    
    public FactorMatrix(FactorMatrix copy){
        super(copy.getRowLength(),copy.getColLength());
        double[][] copyEntry = copy.getEntry();
        for (int i = 0; i < copyEntry.length; i++) {
            for (int j = 0; j < copyEntry[i].length; j++) {
                this.entry[i][j] = copyEntry[i][j];
            }
        }
    }
    
    public FactorMatrix(int row, int col, FactorType t){
        super(row,col);
        this.type = t;
        
        //initialize entry between 1.0 - 5.0
//        Random r = new Random();
//        Supplier<Double> randomValue = () -> (double) (r.nextInt(5)+1);
//        for (int i = 0; i < entry.length; i++) {
//            entry[i] = Stream.generate(randomValue).limit(entry[i].length).mapToDouble(Double::valueOf).toArray();
//        }   
        
        //pengujian metode 1
        double[][] newEntry = null;
        if(this.type == FactorType.USER){
            newEntry = new double[][]{{4,1},
                {5,1},
                {3,4},
                {5,4}};
        } else if(this.type == FactorType.RECIPES) {
            newEntry = new double[][]{{2,3},
                {1,2},
                {5,2},
                {1,1}};
        } else {
            newEntry = new double[][]{
                {2.0,1.0},
                {1.0,3.0},
                {5.0,3.0},
                {2.0,4.0},
                {5.0,5.0},
                {4.0,4.0},
                {4.0,4.0},
                {2.0,4.0},
                {2.0,5.0},
                {3.0,2.0},
                {4.0,1.0},
                {3.0,5.0},    
            };
        }
       this.entry = newEntry;
    }
    
    // unique for ingredient recipe matrix map
    public FactorMatrix(int row, int col, FactorType t, 
            List<Recipe> recipe,
            HashMap<String, Integer> recipeIdx,
            HashMap<String, Integer> ingredientIdx){
        super(row,col);
        this.type = t;
        
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
    
    public double[] getFactorByIndex(int i){
        return this.entry[i];
    }
    
    public double calculateVectorLength(){
        double res = 0.0;
        for (int i = 0; i < entry.length; i++) {
            double currFactor = 0.0;
            for (int j = 0; j < entry[i].length; j++) {
                currFactor += Math.pow(entry[i][j],2);
            }
            res += currFactor;
        }
        return res;
    }
    
    public double[][] getEntry(){return this.entry;}
    
    public void optimizeAllValue(){
        for (int i = 0; i < entry.length; i++) {
            for (int j = 0; j < entry[j].length; j++) {
                if(entry[i][j]>5.0) entry[i][j] = 5.0;
                else if(entry[i][j]<0.0) entry[i][j] = 0.0;
            }
        }
    }
}
