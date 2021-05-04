/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Recipe;
import Model.User;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.event.ActionEvent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author asus
 */
public class MainDocumentController implements Initializable {

    @FXML
    private TabPane mainTable;

    @FXML
    private Label programStatus;

    @FXML
    private TableView<Recipe> recipes_table;

    @FXML
    private TableView<User> user_table;

    @FXML
    private TableColumn<Recipe, String> recipeCol;

    @FXML
    private TableColumn<Recipe, String> ingredientsCol;

    @FXML
    private TableColumn<Recipe, String> ratingCol;

    @FXML
    private TableColumn<User, Integer> userId_col;

    @FXML
    private TableColumn<User, String> action_col;

    @FXML
    private TextField userId_choice;
    
    @FXML
    private TextArea log_field;

    @FXML
    private Label check_userId_input;
    
    @FXML
    private Button startBtn;

    private ObservableList<Recipe> data_recipe;
    private ObservableList<User> data_user;

    private final Factorization f = new Factorization(this);

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.mainTable.setDisable(true);
        this.programStatus.setText("reading data...");
        boolean loadRecipeData = f.readRecipesData();
        boolean loadUserData = f.readUsersData();
        if (loadRecipeData && loadUserData) {
            this.programStatus.setText("success reading view data");
            this.initializeTable();
        } else {
            this.programStatus.setText("something wrong when reading dataset");
        }
        this.mainTable.setDisable(!(loadRecipeData && loadUserData));
    }

    @FXML
    private void startFactorization(ActionEvent event) {
        String t = this.userId_choice.getText();
        if (t.length() < 1) {
            check_userId_input.setText("Error: Anda tidak mengisi data");
        } else {
            programStatus.setText("In-process of factorization");
            check_userId_input.setText("");
            f.setUserId(t);
            startBtn.setDisable(true);
            // start the program...
            // using thread so that the UI is responding
            Thread mainThread = new Thread(f);
            mainThread.start();
        }
    }

    private void initializeTable() {
        // recipe table data
        this.data_recipe = FXCollections.observableList(f.getRecipes());
        this.recipeCol.setCellValueFactory(
                new PropertyValueFactory<Recipe, String>("name"));
        this.ingredientsCol.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getIngredient())
        );
        this.ratingCol.setCellValueFactory(
                new PropertyValueFactory<Recipe, String>("userRating"));

        this.recipes_table.setItems(this.data_recipe);

        // user id data
        this.data_user = FXCollections.observableList(f.getUsers());
        this.userId_col.setCellValueFactory(
                new PropertyValueFactory<User, Integer>("id"));
        this.action_col.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        
        Callback<TableColumn<User, String>, TableCell<User, String>> cellFactory
                = //
                new Callback<TableColumn<User, String>, TableCell<User, String>>() {
            @Override
            public TableCell call(final TableColumn<User, String> param) {
                
                final TableCell<User, String> cell = new TableCell<User, String>() {

                    final Button btn = new Button("Pilih");
                    
                    
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            btn.setOnAction(event -> {
                                User u = getTableView().getItems().get(getIndex());
                                userId_choice.setText(u.getId());
                                check_userId_input.setText("");
                            });
                            setGraphic(btn);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };
        
        action_col.setCellFactory(cellFactory);
        
        this.user_table.setItems(this.data_user);
    }
    
    public void insertLog(String s){
        this.log_field.appendText(s);
    }
}
