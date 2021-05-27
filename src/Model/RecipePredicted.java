/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author asus
 * @about: model class to be sorted to help finding top-10 recipe
 */

// used for String[] topTenRecipe method in factorizationUtil class
// if that method's not used then this class can be deleted
//public class RecipePredicted implements Comparable<RecipePredicted>{
//    private double predictedValue;
//    private int index;
//    
//    public RecipePredicted(double d, int i){
//        this.predictedValue = d;
//        this.index = i;
//    }
//
//    @Override
//    public int compareTo(RecipePredicted o) {
//        if(this.predictedValue > o.getValue()) return -1;
//        else if(this.predictedValue < o.getValue()) return 1;
//        else return 0;
//    }   
//    
//    public double getValue(){return this.predictedValue;}
//    public int getIndex(){return this.index;}
//}

public class RecipePredicted{
    private SimpleStringProperty recipeName;
    private SimpleStringProperty actualRating;
    private SimpleStringProperty firstRating;
    private SimpleStringProperty secondRating;
    
    public RecipePredicted(String... data){
        this.recipeName = new SimpleStringProperty(data[0]);
        this.actualRating = new SimpleStringProperty(data[1]);
        this.firstRating = new SimpleStringProperty(data[2]);
        this.secondRating = new SimpleStringProperty(data[3]);
    }
    
    public String getRecipeName(){return this.recipeName.get();}
    public String getActualRating(){return this.actualRating.get();}
    public String getFirstRating(){return this.firstRating.get();}
    public String getSecondRating(){return this.secondRating.get();}
    
    public void setRecipeName(String s){this.recipeName.set(s);}
    public void setActualRating(String s){this.actualRating.set(s);}
    public void setFirstRating(String s){this.firstRating.set(s);}
    public void setSecondRating(String s){this.secondRating.set(s);}

    public double getFirstRatingAsDouble(){return Double.parseDouble(this.firstRating.get());}
    public double getSecondRatingAsDouble(){return Double.parseDouble(this.secondRating.get());}
}