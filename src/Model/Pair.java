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
    private String p1; //user
    private String p2; //recipe
    
    public Pair(String s1, String s2){
        this.p1 = s1;
        this.p2 = s2;
    }
    
    public void setPair(String s1, String s2){
        this.p1 = s1;
        this.p2 = s2;
    }
    
    public String[] getPair(){
        return new String[] {this.p1,this.p2};
    }
    
    public String getUser(){return p1;}
    public String getRecipe(){return p2;}
}
