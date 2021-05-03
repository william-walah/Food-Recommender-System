/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;

/**
 *
 * @author asus
 */
public class User {
    private final SimpleStringProperty id;
    //private Button b;
    
    public User(String id){
        this.id = new SimpleStringProperty(id);
        //this.b = new Button("Pilih");   
    }
    
    public String getId(){return id.get();}
    public void setId(String i){this.id.set(i);}
    //public Button getButton(){return b;}
    //public void setButton(Button b){this.b = b;}
    
    
    
}
