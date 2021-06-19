package Model;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 *
 * @author William Walah - 2017730054
 */
public class FactorMatrix extends Matrix{
    
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
        
        //initialize entry between 1.0 - 5.0
        Random r = new Random();
        Supplier<Double> randomValue = () -> (double) (r.nextInt(5)+1);
        for (int i = 0; i < entry.length; i++) {
            entry[i] = Stream.generate(randomValue).limit(entry[i].length).mapToDouble(Double::valueOf).toArray();
        }   
    }
    
    // unique for ingredient recipe matrix map
    public FactorMatrix(int row, int col,
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
