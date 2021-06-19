package Model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author William Walah - 2017730054
 */
public class User {
    private final SimpleStringProperty id;
    
    public User(String id){
        this.id = new SimpleStringProperty(id);
    }
    
    public String getId(){return id.get();}
    public void setId(String i){this.id.set(i);}
    
}
