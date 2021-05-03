/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author asus
 */
public class FXMain extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try{
            AnchorPane loginPage = (AnchorPane) FXMLLoader.load(FXMain.class.getResource("/View/MainDocumment.fxml"));
            Scene scene = new Scene(loginPage);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Matrix Factorization");
            primaryStage.show();
        } catch (Exception ex) {
            Logger.getLogger(FXMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    
    public void switchScene() {
    }
}
