package application.controller;

import application.TreeApp;
import application.model.Model;
import application.model.sites.SiteParser;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;


public class NewSiteDialogController {

    private Stage newSiteStage;
    private boolean okClicked = false;
    private TreeApp treeApp;
    private Model model;
    private SiteParser parser;


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
        this.parser = treeApp.getParser();
    }

    @FXML
    private void handleCheck(){
        treeApp.showSelectorsChecker();
    }

    //Сохраняем введенные запросы к сайту в файл properties.
    @FXML
    private void handleSave(){
        parser.setProperty("mainUrl", mainUrlField.getText());
        parser.setProperty("testPage", testPageField.getText());
        parser.setProperty("testPageTitle", testPageTitleField.getText());
        parser.setProperty("testIntro", testIntroField.getText());
        parser.setProperty("testIntro2", testIntroField2.getText());
        parser.setProperty("testIntro3", testIntroField3.getText());
        parser.setProperty("testPrice", testPriceField.getText());
        parser.setProperty("testContent", testContentField.getText());
        parser.setProperty("testContent2", testContentField2.getText());
        parser.setProperty("testContent3", testContentField3.getText());
        parser.setProperty("testImageLink", testImageLinkField.getText());
        parser.setProperty("testGalleryItem", testGalleryItemField.getText());
        parser.setProperty("priceIndex", priceIndexField.getText());
        parser.setProperty("samplePage", samplePageField.getText());

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Property files", "*.properties");
        fileChooser.getExtensionFilters().add(extensionFilter);

        File file = fileChooser.showSaveDialog(newSiteStage);
        if(file != null){
            try {
                FileWriter fw = new FileWriter(file);
                parser.storeProperties(fw, "Site config");
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
                parser.loadProperties(fr);
                fr.close();
                mainUrlField.setText(parser.getProperty("mainUrl"));
                testPageField.setText(parser.getProperty("testPage"));
                testPageTitleField.setText(parser.getProperty("testPageTitle"));
                testIntroField.setText(parser.getProperty("testIntro"));
                testIntroField2.setText(parser.getProperty("testIntro2"));
                testIntroField3.setText(parser.getProperty("testIntro3"));
                testPriceField.setText(parser.getProperty("testPrice"));
                testContentField.setText(parser.getProperty("testContent"));
                testContentField2.setText(parser.getProperty("testContent2"));
                testContentField3.setText(parser.getProperty("testContent3"));
                testImageLinkField.setText(parser.getProperty("testImageLink"));
                testGalleryItemField.setText(parser.getProperty("testGalleryItem"));
                priceIndexField.setText(parser.getProperty("priceIndex"));
                samplePageField.setText(parser.getProperty("samplePage"));
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //Обработка нажатия "ОК". Начинается обработка стороннего сайта.
    @FXML
    private void handleOk(){
        parser.setProperty("mainUrl", mainUrlField.getText());
        parser.setProperty("testPage", testPageField.getText());
        parser.setProperty("testPageTitle", testPageTitleField.getText());
        parser.setProperty("testIntro", testIntroField.getText());
        parser.setProperty("testIntro2", testIntroField2.getText());
        parser.setProperty("testIntro3", testIntroField3.getText());
        parser.setProperty("testPrice", testPriceField.getText());
        parser.setProperty("testContent", testContentField.getText());
        parser.setProperty("testContent2", testContentField2.getText());
        parser.setProperty("testContent3", testContentField3.getText());
        parser.setProperty("testImageLink", testImageLinkField.getText());
        parser.setProperty("testGalleryItem", testGalleryItemField.getText());
        parser.setProperty("priceIndex", priceIndexField.getText());
        parser.setProperty("samplePage", samplePageField.getText());
        try {
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
