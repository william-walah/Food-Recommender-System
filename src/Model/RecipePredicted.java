/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author asus
 * @about: model class to be sorted to help finding top-10 recipe
 */
public class RecipePredicted implements Comparable<RecipePredicted>{
    private double predictedValue;
    private int index;
    
    public RecipePredicted(double d, int i){
        this.predictedValue = d;
        this.index = i;
    }

    @Override
    public int compareTo(RecipePredicted o) {
        if(this.predictedValue > o.getValue()) return -1;
        else if(this.predictedValue < o.getValue()) return 1;
        else return 0;
    }
    
    public double getValue(){return this.predictedValue;}
    public int getIndex(){return this.index;}
}
