/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Recipe;
import Model.User;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author asus
 */
public class Factorization {
    private List<Recipe> recipes;
    private List<User> users;
   
    public Factorization(){
        this.recipes = new ArrayList<Recipe>(); 
        this.users = new ArrayList<User>(); 
    }
    
    public boolean readRecipesData(){
        boolean success = false;
        try {
            File file = new File("src/data/dataset_recipes_ingredient_list_readable_java.csv");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            String[] tempArr;
            while((line = br.readLine()) != null) {
               tempArr = line.split("[|]+");
               for(String t: tempArr){
                   System.out.println(t);
               }
               this.recipes.add(new Recipe(Integer.parseInt(tempArr[0]),tempArr[1],tempArr[2]));
            }
            br.close();
            success = true;
         } catch(IOException ioe) {
            ioe.printStackTrace();
         } finally {
            if(!this.recipes.isEmpty()){
                this.recipes.forEach((e) -> {
                    System.out.println(e.getId()+","+e.getName());
                });
            }
            return success;
        }
    }
    
    public boolean readUsersData(){
        boolean success = false;
        try {
            File file = new File("src/data/dataset_userId_list.csv");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            String[] tempArr;
            while((line = br.readLine()) != null) {
               this.users.add(new User(line));
            }
            br.close();
            success = true;
         } catch(IOException ioe) {
            ioe.printStackTrace();
         } finally {
            if(!this.recipes.isEmpty()){
                this.recipes.forEach((e) -> {
                    System.out.println(e.getId()+","+e.getName());
                });
            }
            return success;
        }
    }
    
    public List<Recipe> getRecipes(){return this.recipes;}
    
    public List<User> getUsers(){return this.users;}
}
