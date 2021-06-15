/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Recipe;
import Model.RecipePredicted;
import Model.User;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.application.Platform;
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
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
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
    private TableView<Recipe> custom_user_table;
    
    @FXML
    private TableColumn<Recipe, String> cu_recipesCol;

    @FXML
    private TableColumn<Recipe, String> cu_ingredientsCol;

    @FXML
    private TableColumn<Recipe, String> cu_ratingCol;

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
    private TableView<RecipePredicted> customize_result;

    @FXML
    private TableColumn<RecipePredicted, String> cust_recipeName;
    
    @FXML
    private TableColumn<RecipePredicted, String> cust_actual;

    @FXML
    private TableColumn<RecipePredicted, String> cust_predicted1;

    @FXML
    private TableColumn<RecipePredicted, String> cust_predicted2;
    
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
    private Button custom_recom;
    
    @FXML
    private Button param_save;
    
    @FXML
    private ToggleGroup learning_rate;

    @FXML
    private Label max_recipe;

    @FXML
    private TextField LR_Value;

    @FXML
    private TextField latent_size;

    @FXML
    private TextField max_iteration;

    @FXML
    private TextField lambda_value;
    
    @FXML
    private Pane top_n_container;

    @FXML
    private TextField top_n_input;

    @FXML
    private TextArea top_n_ta;
    
    @FXML
    private Label user_recom_info;
    
    private ObservableList<Recipe> data_recipe;
    private ObservableList<User> data_user;

    private Factorization firstProcess = new Factorization(this);
    private Factorization secondProcess;
    private String userIdChoice;
    private String LR_type = "Fixed";
    private boolean isParameterSet = false;
    private boolean isFirstProcessFactorized = false;
    private boolean isSecondProcessFactorized = false;
    private boolean isInProcess = false;
    private final int MAX_INT = 1000000;
    private int RECIPE_SIZE = 0;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.mainTable.setDisable(true);
        this.programStatus.setText("Membac dataset...");
        boolean isPreprocessed = firstProcess.isPreprocessed();
        this.RECIPE_SIZE = firstProcess.getRecipeSize();
        this.max_recipe.setText("Max. "+RECIPE_SIZE);
        this.top_n_container.setDisable(true);
        if (isPreprocessed) {
            this.programStatus.setText("Berhasil membaca & menginisialisasi dataset");
            secondProcess = firstProcess.copy();
            this.initializeTable();
        } else {
            this.programStatus.setText("Terjadi kesalahan dalam proses membaca atau inisialisasi dataset. Lihat riwayat aktivitas program untuk lebih detail.");
        }
        this.mainTable.setDisable(!(isPreprocessed));
        
        learning_rate.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                Toggle old_toggle, Toggle new_toggle) {
                if (learning_rate.getSelectedToggle() != null) {
                    RadioButton selectedRadioButton = (RadioButton) learning_rate.getSelectedToggle();
                    LR_type = selectedRadioButton.getText();
                    if(selectedRadioButton.getText().equals("Fixed")) disableField(LR_Value,false);
                    else disableField(LR_Value,true);
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
        latent_size.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                {
                    String newValue = latent_size.getText().replaceFirst("^0+(?!$)", "");
                    if(newValue.length() > 7) newValue = MAX_INT+"";
                    else if(newValue.length() == 7 && Integer.parseInt(newValue) > MAX_INT) newValue = MAX_INT+"";
                    else newValue = newValue.equals("") || Integer.parseInt(newValue) < 2 ? "2" : Integer.parseInt(newValue)+"";
                    latent_size.setText(newValue);
                }
            }
        });
        
        max_iteration.setTextFormatter(new TextFormatter<>(change -> {
            if(change.getText().matches("([0-9]*)?")){
                return change;
            }
            return null;
        }));
        max_iteration.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                {
                    String newValue = max_iteration.getText().replaceFirst("^0+(?!$)", "");
                    if(newValue.length() > 7) newValue = MAX_INT+"";
                    else if(newValue.length() == 7 && Integer.parseInt(newValue) > MAX_INT) newValue = MAX_INT+"";
                    else newValue = newValue.equals("") || newValue.equals("0") ? "1" : Integer.parseInt(newValue)+"";
                    max_iteration.setText(newValue);
                }
            }
        });
        
        top_n_input.setTextFormatter(new TextFormatter<>(change -> {
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
        lambda_value.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                {
                    if(lambda_value.getText().equals("")) {
                        lambda_value.setText("0.000001");
                        return;
                    }
                    String[] currValue = lambda_value.getText().split("\\.");
                    currValue[0] = currValue[0].replaceFirst("^0+(?!$)", "");
                    String frontValue = currValue[0];
                    if(currValue[0].length() >= 7){
                        frontValue = MAX_INT+"";
                    } 
                    
                    String backValue = "";
                    if(currValue.length > 1){
                        backValue = currValue[1];
                        if(backValue.length()>6) {
                            backValue = "000001";
                        }
                    } else {    
                        backValue = "0";
                    }
                    
                    String newValue = frontValue+"."+backValue;
                    lambda_value.setText(newValue);
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
        LR_Value.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                {
                    if(LR_Value.getText().equals("")) {
                        LR_Value.setText("0.000001");
                        return;
                    }
                    String[] currValue = LR_Value.getText().split("\\.");
                    currValue[0] = currValue[0].replaceFirst("^0+(?!$)", "");
                    String frontValue = currValue[0];
                    if(currValue[0].length() >= 7){
                        frontValue = MAX_INT+"";
                    } 
                    
                    String backValue = "";
                    if(currValue.length > 1){
                        backValue = currValue[1];
                        if(backValue.length()>6) {
                            backValue = "000001";
                        }
                    } else {    
                        backValue = "0";
                    }
                    
                    String newValue = frontValue+"."+backValue;
                    LR_Value.setText(newValue);
                }
            }
        });
    }

    @FXML
    private void saveParameter(ActionEvent event){
        Boolean[] check = new Boolean[4];
        int latent_size_val = !latent_size.getText().equals("") ? Integer.parseInt(latent_size.getText()) : 0;
        int max_iteration_val = !max_iteration.getText().equals("") ? Integer.parseInt(max_iteration.getText()) : 0;
        double lambda_val = !lambda_value.getText().equals("") ? Double.parseDouble(lambda_value.getText()) : 0.0;
        double learning_rate_val = !LR_Value.getText().equals("") ? Double.parseDouble(LR_Value.getText()) : 0.0;

        check[0] = latent_size_val > 0 && latent_size_val <= MAX_INT
                    ? true
                    : false;
        check[1] = max_iteration_val > 0 && max_iteration_val <= MAX_INT
                    ? true
                    : false;
        check[2] = lambda_val > 0.0 && lambda_val < (MAX_INT+1)*1.0
                    ? true 
                    : false;
        check[3] = LR_type.equals("Fixed") 
                    ? learning_rate_val > 0.0 && learning_rate_val < (MAX_INT+1)*1.0
                        ? true
                        : false
                    : true;
        
        if(Arrays.asList(check).contains(false)){
            //some field is empty   
            if(!check[0]) latent_size.getStyleClass().add("error_input");
            if(!check[1]) max_iteration.getStyleClass().add("error_input");
            if(!check[2]) lambda_value.getStyleClass().add("error_input");
            if(!check[3]) LR_Value.getStyleClass().add("error_input");
            this.programStatus.setText("PERHATIAN!! Beberapa input parameter masih kosong atau tidak sesuai kriteria.");
        } else {
            latent_size.getStyleClass().remove("error_input");
            max_iteration.getStyleClass().remove("error_input");
            lambda_value.getStyleClass().remove("error_input");
            LR_Value.getStyleClass().remove("error_input");
            
            this.programStatus.setText("Parameter berhasil disimpan.");
            insertLog("# Berhasil menyimpan nilai parameter: \n"
                    + "#   - Latent Size        : "+latent_size.getText()+"\n"
                    + "#   - Max Iteration      : "+max_iteration.getText()+"\n"
                    + "#   - Lambda Value       : "+lambda_value.getText()+"\n"
                    + "#   - Learning Rate Type : "+LR_type+"\n"
                    + "#   - Learning Rate Value: "+(LR_type.equals("Fixed") ? LR_Value.getText() : "1/t")+"\n"
                    );
            this.isParameterSet = true;
            
            //set parameter for both Factorization Object
            firstProcess.setParameter(
                    latent_size.getText(),
                    max_iteration.getText(),
                    lambda_value.getText(),
                    LR_Value.getText(),
                    LR_type
            );
            secondProcess.setParameter(
                    latent_size.getText(),
                    max_iteration.getText(),
                    lambda_value.getText(),
                    LR_Value.getText(),
                    LR_type
            );
        }
    }
    
    @FXML
    private void startFactorization(ActionEvent event) {
        if(isParameterSet && !isInProcess) {
            programStatus.setText("Proses faktorisasi untuk dataset sedang berjalan...");
            isInProcess = true;
            startBtn.setDisable(true);
            custom_recom.setDisable(true);
            param_save.setDisable(true);
            // start the program...
            // using thread so that the UI is responding
            Thread mainThread = new Thread(firstProcess);
            mainThread.start();
            Thread afterFactorizationThread = new Thread(){
                @Override
                public void run(){
                    try{
                        while(mainThread.getState() != Thread.State.TERMINATED){
                            Thread.sleep(1000);
                        }
                        startBtn.setDisable(false);
                        custom_recom.setDisable(false);
                        param_save.setDisable(false);
                        isFirstProcessFactorized = true;
                        isInProcess = false;
                        updateProgramStatus("Proses faktorsiasi untuk dataset selesai.");
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            };
            afterFactorizationThread.start();
        } else{
            if(isInProcess) this.programStatus.setText("PERHATIAN!! Proses faktorisasi lain sedang berjalan.");
            else this.programStatus.setText("PERHATIAN!! Parameter proses faktorisasi belum tersimpan.");
        }
    }

    @FXML
    private void doCustomRecommendation(ActionEvent event) {
        if(isParameterSet && !isInProcess) {
            programStatus.setText("Proses faktorisasi khusus pengguna sedang berjalan...");
            custom_recom.setDisable(true);
            startBtn.setDisable(true);
            param_save.setDisable(true);
            custom_user_table.setDisable(true);
            isInProcess = true;
            // create hash map of new custom rating
            HashMap<String, String> customRating = new HashMap<String,String>();
            Pattern zero = Pattern.compile("[0](\\.(00|0))?"); //filter non zero
            ObservableList<Recipe> userRating = this.custom_user_table.getItems();
            for(Recipe curr: userRating){
                if(!zero.matcher(curr.getUserRating()).matches()){ //not match regex == not 0 | 0.0 | 0.00
                    customRating.put(curr.getId(),curr.getUserRating());
                }
            }
            // start the program...
            // using thread so that the UI is responding
            if(customRating.size()>=5){
                secondProcess.addCustomUser("-1", customRating);
                top_n_container.setDisable(true);
                Thread mainThread = new Thread(secondProcess);
                mainThread.start();
                Thread afterFactorizationThread = new Thread(){
                    @Override
                    public void run(){
                        try{
                            while(mainThread.getState() != Thread.State.TERMINATED){
                                Thread.sleep(10000);
                            }
                            custom_recom.setDisable(false);
                            startBtn.setDisable(false);
                            param_save.setDisable(false);
                            custom_user_table.setDisable(false);
                            top_n_container.setDisable(false);
                            isSecondProcessFactorized = true;
                            isInProcess = false;
                            //show recommendation
                            updateCustomRecommendationTable();
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                };
                afterFactorizationThread.start();
            } else {
                isInProcess = false;
                custom_recom.setDisable(false);
                startBtn.setDisable(false);
                param_save.setDisable(false);
                custom_user_table.setDisable(false);
                this.programStatus.setText("PERHATIAN!! Tolong masukkan setidaknya 5 buah rating. (Tercatat: "+customRating.size()+")");
            }
        } else{
            if(isInProcess) this.programStatus.setText("PERHATIAN!! Proses faktorisasi lain sedang berjalan.");
            else this.programStatus.setText("PERHATIAN!! Parameter proses faktorisasi belum tersimpan.");
        }
    }
    
    @FXML
    private void loadCustomRating(ActionEvent event) {
        programStatus.setText("Membaca data rating khusus pengguna");
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Comma-Sepparated Files (*.csv)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        Node source = (Node) event.getSource();
        Window theStage = source.getScene().getWindow();
        File file = fileChooser.showOpenDialog(theStage);
        
        if(file != null){
            this.custom_user_table.setDisable(!this.custom_user_table.isDisable());
            Thread t = new Thread(){
                @Override
                public void run(){
                    boolean success = false;
                    try{
                        Thread.sleep(3000);
                        CSVReader csvReader = new CSVReader(new FileReader(file));
                        HashMap<String,String> data = new HashMap<String,String>();
                        String[] line;
                        while ((line = csvReader.readNext()) != null) {
                            data.put(line[0],line[1]); //id,rating
                        }
                        csvReader.close();
                        if(data.size()>0){
                            ObservableList<Recipe> customeRating = custom_user_table.getItems();
                            for(Recipe curr: customeRating){
                                String id = curr.getId();
                                curr.setUserRating(data.get(id));
                            };
                        }
                        success = true;
                    } catch(Exception e){
                        e.printStackTrace();
                    } finally{
                        custom_user_table.refresh();
                        custom_user_table.setDisable(!custom_user_table.isDisable());
                        if(success) updateProgramStatus("Berhasil membaca data rating pengguna");
                        else updateProgramStatus("Gagal dalam proses membaca rating data pengguna. Lihat riwayat aktivitas program untuk lebih detail.");
                    }
                }
            };
            t.start();
        }
    }

    @FXML
    private void saveCustomRating(ActionEvent event) {
        programStatus.setText("Menyimpan data rating khusus pengguna.");
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Comma-Sepparated Files (*.csv)", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        Node source = (Node) event.getSource();
        Window theStage = source.getScene().getWindow();
        File file = fileChooser.showSaveDialog(theStage);

        if (file != null) {
            //do file save process
            boolean success = false;
            ObservableList<Recipe> customeRating = this.custom_user_table.getItems();
            List<String[]> dataCsv = new ArrayList<String[]>();
            for(Recipe curr: customeRating){
                String[] d = new String[2]; 
                d[0] = curr.getId();
                d[1] = curr.getUserRating();
                dataCsv.add(d);
            }
            try{
                CSVWriter writer = new CSVWriter(new FileWriter(file, false));
                for (String[] s: dataCsv) {
                    writer.writeNext(s);
                }
                writer.close();
                success = true;
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                if(success) programStatus.setText("Berhasil menyimpan data rating. Silahkan lihat area log untuk alamat penyimpanan.");
                else programStatus.setText("Gagal dalam proses menyimpan data rating. Lihat riwayat aktivitas program untuk lebih detail.");
            }
            insertLog("Tersimpan pada: "+file.getAbsolutePath());
        } else {
            programStatus.setText("Gagal dalam proses menyimpan data rating. Lihat riwayat aktivitas program untuk lebih detail.");
        }
    }
     
    @FXML
    private void viewTopRecommendation(ActionEvent event){
        int input = Integer.parseInt(top_n_input.getText());
        if(input > secondProcess.getRecipeSize()){
            this.programStatus.setText("Jumlah rekomendasi yang di inginkan melebihi banyak resep. Berikan input yang sesuai.");
        }  else if(input < 1){
            this.programStatus.setText("Jumlah rekomendasi yang di inginkan adalah 0. Berikan input setidaknya 1.");
        }
        else {
            this.programStatus.setText("");
            String str = "Top-"+top_n_input.getText()+" rekomendasi anda:\n";
            // copy instance
            ObservableList<RecipePredicted> tableList = this.customize_result.getItems();
            List<RecipePredicted> listOfPrediction = new ArrayList<>();
            for(RecipePredicted curr: tableList){
                listOfPrediction.add(new RecipePredicted(
                        curr.getRecipeName(),
                        curr.getActualRating(),
                        curr.getFirstRating(),
                        curr.getSecondRating()
                ));
            }
            Comparator<RecipePredicted> compareFirstRating = (o1, o2) -> Double.compare(o2.getFirstRatingAsDouble(), o1.getFirstRatingAsDouble());
            Comparator<RecipePredicted> compareSecondRating = (o1, o2) -> Double.compare(o2.getSecondRatingAsDouble(), o1.getSecondRatingAsDouble());
            str +="\n";
            //sorted by first method rating
            Collections.sort(listOfPrediction, compareFirstRating);
            str +="   1. Berdasarkan Metode Rekomendasi Pertama:\n";
            int count = 0;
            for(RecipePredicted curr: listOfPrediction){
                if(count==input) break;
                str+="     - "+curr.getRecipeName()+", "+curr.getFirstRating()+"\n";
                count++;
            }
            str+="\n";
            //sorted by first method rating
            Collections.sort(listOfPrediction, compareSecondRating);
            str +="   2. Berdasarkan Metode Rekomendasi Kedua:\n";
            count = 0;
            for(RecipePredicted curr: listOfPrediction){
                if(count==input) break;
                str+="     - "+curr.getRecipeName()+", "+curr.getSecondRating()+"\n";
                count++;
            }
            final String res = str;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    String time = (dtf.format(now));
                    top_n_ta.appendText("~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                    top_n_ta.appendText("# Time: "+time+"\n");
                    top_n_ta.appendText(res);
                    top_n_ta.appendText("~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
                }
            });
        }
    }
    
    @FXML
    private void automaticTesting(ActionEvent event){
//        String[] vectorLength = new String[] {"2","3","4","5","6","7","8","9","10"};
//        String[] maxIteration = new String[] {"100","500","1000"};
//        String[] lambdaValue = new String[] {"0.5","1","2","3"};
//        String[] lrValue = new String[] {"0.1","0.01","0.001","0.0001","0.00001"};

        String[] vectorLength = new String[] {"50","100"};
        String[] maxIteration = new String[] {"400"};
        String[] lambdaValue = new String[] {"0.1"};
        String[] lrValue = new String[] {"0.001","0.0001"};

//        String[] vectorLength = new String[] {"2"};
//        String[] maxIteration = new String[] {"1000"};
//        String[] lambdaValue = new String[] {"0.1","0.5","1","2","3"};
//        String[] lrValue = new String[] {"0.1","0.01","0.001","0.0001","0.00001"};

        ((Button)event.getSource()).setDisable(true);
        String lrType = "Fixed";
        Thread automaticTest = new Thread(){
            @Override
            public void run(){
                startBtn.setDisable(true);
                int count = 1;
                custom_recom.setDisable(true);
                try{
                    for(String vector: vectorLength){
                        for(String iterate: maxIteration){
                            for(String lambda: lambdaValue){
                                for(String lr: lrValue){
                                    firstProcess.setParameter(vector,iterate,lambda,lr,lrType);
                                    System.out.println("Proses ke-"+count);
                                    System.out.printf("Mulai proses untuk parameter: [%s, %s, %s, %s]\n",vector,iterate,lambda,lr);
                                    insertLog(String.format("Mulai proses untuk parameter: [%s, %s, %s, %s]\n",vector,iterate,lambda,lr));
                                    Thread mainThread = new Thread(firstProcess);
                                    mainThread.start();
                                    while(mainThread.getState() != Thread.State.TERMINATED){
                                        Thread.sleep(1000);
                                    }
                                    try{
                                        double[] rmse1 = firstProcess.getFirstRMSE();
                                        double[] rmse2 = firstProcess.getSecondRMSE();
                                        long elapsedTime_1 = firstProcess.getTime(1);
                                        long elapsedTime_2 = firstProcess.getTime(2);
                                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM_HH_mm");  
                                        LocalDateTime now = LocalDateTime.now();  
                                        String time = (dtf.format(now));
                                        File myObj = new File("C:\\Users\\asus\\Desktop\\Result\\n = "+vector+"\\result "+time+".txt");
                                        myObj.createNewFile();
                                        FileWriter writer = new FileWriter(myObj);
                                        String s = String.format("Hasil dengan parameter [%s, %s, %s, %s]:\n",vector,iterate,lambda,lr)
                                            + "############################\n"+ "############################\n"
                                            + String.format("# RMSE (Test  Set) = %.3f\n",rmse1[0])
                                            + String.format("# RMSE (Train Set) = %.3f\n",rmse1[1])
                                            + String.format("# RMSE (Data  Set) = %.3f\n",rmse1[2])
                                            + String.format("# Waktu Berjalan: %.3fs\n", elapsedTime_1/1000F)
                                            + "############################\n"
                                            + "\n"
                                            + "############################\n"
                                            + "# SECOND OPTIMIZATION METHOD\n"
                                            + String.format("# RMSE (Test  Set) = %.5f\n",rmse2[0])
                                            + String.format("# RMSE (Train Set) = %.5f\n",rmse2[1])
                                            + String.format("# RMSE (Data  Set) = %.5f\n",rmse2[2])
                                            + String.format("# Waktu Berjalan: %.3fs\n", elapsedTime_2/1000F)
                                            + "############################\n";
                                        writer.write(s);
                                        writer.close();
                                    } catch(IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.printf("proses selesai untuk parameter: [%s, %s, %s, %s]\n",vector,iterate,lambda,lr);
                                    insertLog(String.format("proses selesai untuk parameter: [%s, %s, %s, %s]\n",vector,iterate,lambda,lr));
                                    count++;
                                }
                            }
                        }
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                ((Button)event.getSource()).setDisable(false);
                startBtn.setDisable(false);
                custom_recom.setDisable(false);
            }
        };
        automaticTest.start();
    }
    
    private void initializeTable() {
        /*
            custom user rating recipe table
        */
        this.data_recipe = FXCollections.observableList(firstProcess.getRecipes());
        this.cu_recipesCol.setCellValueFactory(
                new PropertyValueFactory<Recipe, String>("name"));
        this.cu_ingredientsCol.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getIngredient())
        );
        // editable column
        this.cu_ratingCol.setCellValueFactory(
                new PropertyValueFactory<Recipe, String>("userRating"));
        // thanks to the person from referable link on the RatingEditingCell inner class
        this.cu_ratingCol.setCellFactory(col -> new RatingEditingCell());
        this.custom_user_table.setItems(this.data_recipe);

        
        /*
            dataset user id list table
        */
        this.data_user = FXCollections.observableList(firstProcess.getUsers());
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
                                if(isFirstProcessFactorized){
                                    User u = getTableView().getItems().get(getIndex());
                                    userIdChoice = u.getId();
                                    updateRecommendationTable();
                                    user_recom_info.setText("Sedang Menampilkan Rekomendasi User: "+u.getId());
                                } else {
                                    programStatus.setText("Faktorisasi untuk dataset belum pernah dijalankan. Tidak dapat memberikan rekomendasi.");
                                }
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
        Platform.runLater(() -> {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
            LocalDateTime now = LocalDateTime.now();  
            String time = (dtf.format(now));
            this.log_field.appendText("~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
            this.log_field.appendText("# Time: "+time+"\n");
            this.log_field.appendText(s);
            this.log_field.appendText("~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
        });
    }
    
    public void updateProgramStatus(String s){
        Platform.runLater(()-> {
            this.programStatus.setText(s);
        });
    }
    
    private void disableField(TextField t, boolean isDisable){
        t.setDisable(isDisable);
    }
    
    public void updateRecommendationTable(){
        // thanks: https://www.stackoverflow.com/question/18971109/javafx-tableview-not-showing-data-in-all-columns
        // for pointing observablelist attribute naming
        this.recommendation_table.setDisable(true);
        ObservableList<RecipePredicted> data_ratingPrediction = FXCollections.observableList(firstProcess.getUserPrediction(userIdChoice));
        
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
    
    public void updateCustomRecommendationTable(){
        // thanks: https://www.stackoverflow.com/question/18971109/javafx-tableview-not-showing-data-in-all-columns
        // for pointing observablelist attribute naming
        this.customize_result.setDisable(true);
        ObservableList<RecipePredicted> data_ratingPrediction = FXCollections.observableList(secondProcess.getUserPrediction("-1"));
        
        this.cust_recipeName.setCellValueFactory(
                new PropertyValueFactory<RecipePredicted, String>("recipeName"));

        this.cust_actual.setCellValueFactory(
                new PropertyValueFactory<RecipePredicted, String>("actualRating"));
        
        this.cust_predicted1.setCellValueFactory(
                new PropertyValueFactory<RecipePredicted, String>("firstRating"));
    
        this.cust_predicted2.setCellValueFactory(
                new PropertyValueFactory<RecipePredicted, String>("secondRating"));
    
        this.customize_result.setItems(data_ratingPrediction);
        this.customize_result.setDisable(false);
    }
    
//   public void afterFactorization(){
//        this.recom_tab.setDisable(false);
//        this.custom_recom_tab.setDisable(false);
//        this.isFactorized = true;
//    }
    
    
    //thanks to: https://stackoverflow.com/questions/27900344/how-to-make-a-table-column-with-integer-datatype-editable-without-changing-it-to
    //for class TableCell<S,T> information
    class RatingEditingCell extends TableCell<Recipe, String> {

        private final TextField textField = new TextField();
        //thanks to: https://forums.asp.net/t/1427365.aspx?regex+non+negative+decimal+with+at+least+4+decimal+places
        //for giving information
        private final Pattern decimalPattern = Pattern.compile("[0-5](\\.([0-9]{0,2}|00|0))?");

        public RatingEditingCell() {
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (! isNowFocused) {
                    processEdit();
                }
            });
            textField.setOnAction(event -> processEdit());
        }

        private void processEdit() {
            String text = textField.getText();
            if (decimalPattern.matcher(text).matches()) {
                String[] number = text.split(".");
                if(number.length != 0){ //single input like 5, 4, 3, 2, 1, 0
                    if(Integer.parseInt(number[0]) == 5 && Integer.parseInt(number[1])>0){
                    // rating like 5.9 or something
                    cancelEdit();
                    } else {
                        commitEdit(text);
                    }
                } else {
                    commitEdit(text);
                }
                
            } else {
                cancelEdit();
            }
        }

        @Override
        public void updateItem(String value, boolean empty) {
            super.updateItem(value, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                setText(null);
                textField.setText(value);
                setGraphic(textField);
            } else {
                setText(value);
                setGraphic(null);
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            String value = getItem();
            if (value != null) {
                textField.setText(value);
                setGraphic(textField);
                setText(null);
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().toString());
            setGraphic(null);
        }

        // This seems necessary to persist the edit on loss of focus; not sure why:
        @Override
        public void commitEdit(String value) {
            super.commitEdit(value);
            ((Recipe)this.getTableRow().getItem()).setUserRating(value);
        }
    }
}
