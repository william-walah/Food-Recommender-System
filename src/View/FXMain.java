package View;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author William Walah - 2017730054
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
}
