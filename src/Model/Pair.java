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
public class Pair {
    private String user; //user
    private String recipe; //recipe
    
    public Pair(String s1, String s2){
        this.user = s1;
        this.recipe = s2;
    }
    
    public void setPair(String s1, String s2){
        this.user = s1;
        this.recipe = s2;
    }
    
    public String[] getPair(){
        return new String[] {this.user,this.recipe};
    }
    
    public String getUser(){return user;}
    public String getRecipe(){return recipe;}
}
