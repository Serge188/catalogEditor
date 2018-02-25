package application.controller;

import application.TreeApp;
import application.model.Model;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Properties;


public class NewSiteDialogController {

    private Stage newSiteStage;
    private boolean okClicked = false;
    private TreeApp treeApp;
    private Model model;
    private Properties properties = new Properties();


    @FXML
    private TextField mainUrlField;

    @FXML
    private TextField testPageField;

    @FXML
    private TextField testPageTitleField;

    @FXML
    private TextField testIntroField;

    @FXML
    private TextField testIntroField2;

    @FXML
    private TextField testIntroField3;

    @FXML
    private TextField testPriceField;

    @FXML
    private TextField priceIndexField;

    @FXML
    private TextField testContentField;

    @FXML
    private TextField testContentField2;

    @FXML
    private TextField testContentField3;

    @FXML
    private TextField testImageLinkField;

    @FXML
    private TextField testGalleryItemField;

    @FXML
    private TextField samplePageField;

    @FXML
    private Button checkButton;

    @FXML
    private Button okButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button openButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label checkTitleLabel;


    public void setStage(Stage stage){
        this.newSiteStage = stage;
    }

    public boolean isOkClicked(){
        return okClicked;
    }

    public void setTreeApp(TreeApp treeApp){
        this.treeApp = treeApp;
        this.model = treeApp.getModel();
    }

    @FXML
    private void handleCheck(){
        treeApp.showSelectorsChecker();
    }

    //Сохраняем введенные запросы к сайту в файл properties.
    @FXML
    private void handleSave(){
        model.setProperty("mainUrl", mainUrlField.getText());
        model.setProperty("testPage", testPageField.getText());
        model.setProperty("testPageTitle", testPageTitleField.getText());
        model.setProperty("testIntro", testIntroField.getText());
        model.setProperty("testIntro2", testIntroField2.getText());
        model.setProperty("testIntro3", testIntroField3.getText());
        model.setProperty("testPrice", testPriceField.getText());
        model.setProperty("testContent", testContentField.getText());
        model.setProperty("testContent2", testContentField2.getText());
        model.setProperty("testContent3", testContentField3.getText());
        model.setProperty("testImageLink", testImageLinkField.getText());
        model.setProperty("testGalleryItem", testGalleryItemField.getText());
        model.setProperty("priceIndex", priceIndexField.getText());
        model.setProperty("samplePage", samplePageField.getText());

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Property files", "*.properties");
        fileChooser.getExtensionFilters().add(extensionFilter);

        File file = fileChooser.showSaveDialog(newSiteStage);
        if(file != null){
            try {
                FileWriter fw = new FileWriter(file);
                model.storeProperties(fw, "Site config");
                fw.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //Открываем файл с запросами к стороннему сайту. Заполняем поля формы данными из файла.
    @FXML
    private void handleOpen(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Property files", "*.properties");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File file = fileChooser.showOpenDialog(newSiteStage);
        if(file != null){
            try {
                FileReader fr = new FileReader(file);
                model.loadProperties(fr);
                fr.close();
                mainUrlField.setText(model.getProperty("mainUrl"));
                testPageField.setText(model.getProperty("testPage"));
                testPageTitleField.setText(model.getProperty("testPageTitle"));
                testIntroField.setText(model.getProperty("testIntro"));
                testIntroField2.setText(model.getProperty("testIntro2"));
                testIntroField3.setText(model.getProperty("testIntro3"));
                testPriceField.setText(model.getProperty("testPrice"));
                testContentField.setText(model.getProperty("testContent"));
                testContentField2.setText(model.getProperty("testContent2"));
                testContentField3.setText(model.getProperty("testContent3"));
                testImageLinkField.setText(model.getProperty("testImageLink"));
                testGalleryItemField.setText(model.getProperty("testGalleryItem"));
                priceIndexField.setText(model.getProperty("priceIndex"));
                samplePageField.setText(model.getProperty("samplePage"));
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //Обработка нажатия "ОК". Начинается обработка стороннего сайта.
    @FXML
    private void handleOk(){
        model.setProperty("mainUrl", mainUrlField.getText());
        model.setProperty("testPage", testPageField.getText());
        model.setProperty("testPageTitle", testPageTitleField.getText());
        model.setProperty("testIntro", testIntroField.getText());
        model.setProperty("testIntro2", testIntroField2.getText());
        model.setProperty("testIntro3", testIntroField3.getText());
        model.setProperty("testPrice", testPriceField.getText());
        model.setProperty("testContent", testContentField.getText());
        model.setProperty("testContent2", testContentField2.getText());
        model.setProperty("testContent3", testContentField3.getText());
        model.setProperty("testImageLink", testImageLinkField.getText());
        model.setProperty("testGalleryItem", testGalleryItemField.getText());
        model.setProperty("priceIndex", priceIndexField.getText());
        model.setProperty("samplePage", samplePageField.getText());
        try {
            //String errorMessage = model.checkQueries();
            if (false) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(newSiteStage);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Пожалуйста, проверьте введенные данные");
                alert.setContentText("");
                alert.showAndWait();
            } else {
                okClicked = true;
                newSiteStage.close();
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(newSiteStage);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Пожалуйста, проверьте введенные данные");
            alert.setContentText("---");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel(){
        newSiteStage.close();
    }
}
