package Controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the e   ditor.
 */

import View.FXMain;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author asus
 */
public class LoginDocumentController implements Initializable {
    
    @FXML
    private TextField user_field;

    @FXML
    private Label err_username;

    @FXML
    private Button login;
   
    
    @FXML   
    private void handleLogin(ActionEvent event) {
        if(user_field.getText().length() > 0){
            try{
                AnchorPane mainPage = (AnchorPane) FXMLLoader.load(FXMain.class.getResource("/View/MainDocumment.fxml"));
                Scene scene = new Scene(mainPage);
                Stage primaryStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                primaryStage.setScene(scene);
                primaryStage.show();
            }
            catch (Exception ex){
                Logger.getLogger(FXMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            err_username.setVisible(true);
        }
    }
    
    @Override   
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
