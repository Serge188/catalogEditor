package application.controller;

import application.TreeApp;
import application.model.Model;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginFormController {
    private TreeApp treeApp;
    private boolean okClicked;
    private Model model;
    private Stage loginStage;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button camcelButton;

    @FXML
    private void handleLogin(){
        String login = loginField.getText();
        String password = passwordField.getText();
        if(!login.isEmpty() && ! password.isEmpty()) {
            okClicked = model.setDao(login, password);
        } else {
            okClicked = false;
        }
        loginStage.close();
    }

    @FXML
    private void handleCancel(){
        okClicked = true;
        loginStage.close();
    }

    @FXML
    private void initialize(){
    }

    public boolean isOkClicked(){
        return okClicked;
    }

    public void setTreeApp(TreeApp treeApp){
        this.treeApp = treeApp;
        this.model = treeApp.getModel();
    }

    public void setStage(Stage stage){
        this.loginStage = stage;
    }

}
