package Model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author William Walah - 2017730054
 */

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