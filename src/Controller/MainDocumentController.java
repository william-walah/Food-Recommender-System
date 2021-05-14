/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Recipe;
import Model.RecipePredicted;
import Model.User;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;

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
    private TableColumn<Recipe, String> recipeCol;

    @FXML
    private TableColumn<Recipe, String> ingredientsCol;

    @FXML
    private TableColumn<Recipe, String> ratingCol;

    @FXML
    private TableView<RecipePredicted> recommendation_table;

    @FXML
    private TableColumn<RecipePredicted, String> recipe_col;

    @FXML
    private TableColumn<RecipePredicted, String> actualRate_col;

    @FXML
    private TableColumn<RecipePredicted, String> predictedRate1_col;
    
    @FXML
    private TableColumn<RecipePredicted, String> predictedRate2_col;

    @FXML
    private TableView<User> user_table;
    
    @FXML
    private TableColumn<User, Integer> userId_col;

    @FXML
    private TableColumn<User, String> action_col;

    @FXML
    private TextArea log_field;

    @FXML
    private Button startBtn;
    
    @FXML
    private ToggleGroup learning_rate;

    @FXML
    private Label LR_label;

    @FXML
    private TextField LR_Value;

    @FXML
    private TextField latent_size;

    @FXML
    private TextField max_iteration;

    @FXML
    private TextField lambda_value;
     
    @FXML
    private Tab recom_tab;
    
    @FXML
    private Tab custom_recom_tab;
    
    private ObservableList<Recipe> data_recipe;
    private ObservableList<User> data_user;
    private ObservableList<RecipePredicted> data_ratingPrediction;

    private final Factorization f = new Factorization(this);
    private String userIdChoice;
    private String LR_type;

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
        
        learning_rate.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                Toggle old_toggle, Toggle new_toggle) {
                if (learning_rate.getSelectedToggle() != null) {
                    RadioButton selectedRadioButton = (RadioButton) learning_rate.getSelectedToggle();
                    LR_type = selectedRadioButton.getText();
                    alterLabelField(selectedRadioButton.getText());
                }
            }
        });
        
        // make textfield limited to number only
        // thanks: https://www.youtube.com/watch?v=n5bVGAJ8KkE
        latent_size.setTextFormatter(new TextFormatter<>(change -> {
            if(change.getText().matches("([0-9]*)?")){
                return change;
            }
            return null;
        }));
        max_iteration.setTextFormatter(new TextFormatter<>(change -> {
            if(change.getText().matches("([0-9]*)?")){
                return change;
            }
            return null;
        }));
        lambda_value.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.matches("\\d*(\\.\\d*)?")) {
                    lambda_value.setText(oldValue);
                }
            }
        });
        LR_Value.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.matches("\\d*(\\.\\d*)?")) {
                    LR_Value.setText(oldValue);
                }
            }
        });
    }

    @FXML
    private void startFactorization(ActionEvent event) {
        Boolean[] check = new Boolean[4];
        check[0] = latent_size.getText().length() < 1 ? false : true;
        check[1] = max_iteration.getText().length() < 1 ? false : true;
        check[2] = lambda_value.getText().length() < 1 ? false : true;
        check[3] = LR_type.equals("Fixed") 
                ? LR_Value.getText().length() < 1
                    ? false
                    : true
                : true;
        if(Arrays.asList(check).contains(false)){
            //some field is empty   
            this.programStatus.setText("Can't start factorization process, some parameter field is empty.");
        } else {
            //truep
            programStatus.setText("In-process of factorization");
            //f.setUserId(userIdChoice);
            startBtn.setDisable(true);
            // start the program...
            // using thread so that the UI is responding
            f.setParameter(
                    latent_size.getText(),
                    max_iteration.getText(),
                    lambda_value.getText(),
                    LR_Value.getText(),
                    LR_type
            );
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
                                userIdChoice = u.getId();
                                updateRecommendationTable();
                            });
                            setGraphic(btn);
                            setText(null);
                        }
                    }
                };
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        };

        action_col.setCellFactory(cellFactory);

        this.user_table.setItems(this.data_user);
    }

    public void insertLog(String s) {
        this.log_field.appendText(s);
    }
    
    private void alterLabelField(String text){
        if(text.equals("Fixed")) {
            LR_label.setText("Nilai Learning Rate");
            LR_label.setVisible(true);
            LR_Value.setVisible(true);
        }
        else {
            LR_label.setVisible(false);
            LR_Value.setVisible(false);
        }
        
    }
    
    public void updateRecommendationTable(){
        // thanks: https://www.stackoverflow.com/question/18971109/javafx-tableview-not-showing-data-in-all-columns
        // for pointing observablelist attribute naming
        this.recommendation_table.setDisable(true);
        this.data_ratingPrediction = FXCollections.observableList(f.getUserPrediction(userIdChoice));
        
        this.recipe_col.setCellValueFactory(
                new PropertyValueFactory<RecipePredicted, String>("recipeName"));

        this.actualRate_col.setCellValueFactory(
                new PropertyValueFactory<RecipePredicted, String>("actualRating"));

        this.predictedRate1_col.setCellValueFactory(
                new PropertyValueFactory<RecipePredicted, String>("firstRating"));
    
        this.predictedRate2_col.setCellValueFactory(
                new PropertyValueFactory<RecipePredicted, String>("secondRating"));
    
        this.recommendation_table.setItems(data_ratingPrediction);
        this.recommendation_table.setDisable(false);
    }
    
    public void afterFactorization(){
        this.recom_tab.setDisable(false);
        this.custom_recom_tab.setDisable(false);
    }
}
