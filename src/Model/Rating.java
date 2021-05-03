/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author asus
 */
public class Rating {
    private int id;
    private float rating;
    
    public Rating(int id, float rating){
        this.id = id;
        this.rating = rating;
    }
    
    public int getId(){return this.id;}
    public float getRating(){return this.rating;}
    public void setId(int i){this.id = i;}
    public void setRating(float i){this.rating = i;}
}
