package application;

import application.controller.*;
import application.model.Model;
import application.model.ShopItem;
import application.model.sites.SiteParser;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class TreeApp extends Application{
    private Stage stage;
    private BorderPane mainLayout;
    private Model model;
    private SiteParser parser;


    public TreeApp(){
        this.model = new Model();
        this.parser = new SiteParser();
        model.setParser(parser);
        parser.setModel(model);
    }

    public Model getModel(){
        return model;
    }

    public SiteParser getParser() {
        return parser;
    }

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.stage.setTitle("Редактор каталога");

        //При запуске программы показываем главную панель, запрашиваем в цикле учетные данные пользователя
        //Если данные верны, сразу же отображаем дерево товаров, загруженных с основного сайта
        initMainLayout();
        showLoginForm();
        showTree();

    }

    private void initMainLayout(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TreeApp.class.getClassLoader().getResource("MainLayout.fxml"));
            mainLayout = (BorderPane) loader.load();

            Scene scene = new Scene(mainLayout);
            stage.setScene(scene);
            stage.show();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    //Окно ввода данных пользователя
    private boolean showLoginForm(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TreeApp.class.getClassLoader().getResource("LoginForm.fxml"));
            AnchorPane loginForm = (AnchorPane) loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Введите учетные данные");
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(stage);
            Scene scene = new Scene(loginForm);
            loginStage.setScene(scene);
            LoginFormController controller = loader.getController();
            controller.setStage(loginStage);
            controller.setTreeApp(this);
            boolean accepted = false;
            while(!accepted){
                loginStage.showAndWait();
                accepted = controller.isOkClicked();
                if(!accepted){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(loginStage);
                    alert.setTitle("Вход не выполнен");
                    alert.setHeaderText("Указаны неверные имя пользователя/пароль или отсутствует соединение с базой данных");
                    alert.setContentText("");
                    alert.showAndWait();
                }
            }
            return controller.isOkClicked();
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    //Метод выгружает товары с основного сайта  и отображает дерево каталога
    private void showTree(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TreeApp.class.getClassLoader().getResource("TreeOverview.fxml"));
            AnchorPane splitPane = (AnchorPane) loader.load();
            splitPane.setPrefWidth(1200);
            splitPane.setPrefHeight(572);
            mainLayout.setCenter(splitPane);
            TreeOverviewController controller = loader.getController();
            controller.setTreeApp(this);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //Диалоговое окно для при создании / редактировании товара в основном каталоге
    public boolean showNewItemDialog(ShopItem shopItem){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TreeApp.class.getClassLoader().getResource("NewItemDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактирование товара");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            NewItemDialogController controller = loader.getController();
            controller.setNewItemDialogStage(dialogStage);
            controller.setShopItem(shopItem);

            dialogStage.showAndWait();

            return controller.isOkClicked();

        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    //Диалоговое окно для формирования нового запроса к стороннему сайту
    public boolean showNewSiteDialog(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TreeApp.class.getClassLoader().getResource("NewSiteDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage newSiteStage = new Stage();
            newSiteStage.setTitle("Добавление нового сайта для анализа");
            newSiteStage.initModality(Modality.WINDOW_MODAL);
            newSiteStage.initOwner(stage);
            Scene scene = new Scene(page);
            newSiteStage.setScene(scene);
            NewSiteDialogController controller = loader.getController();
            controller.setStage(newSiteStage);
            controller.setTreeApp(this);
            newSiteStage.showAndWait();
            return controller.isOkClicked();
        } catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public void showSelectorsChecker(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TreeApp.class.getClassLoader().getResource("SelectorsChecker.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage selectorStage = new Stage();
            selectorStage.setTitle("Аналз элементов страницы");
            selectorStage.initModality(Modality.WINDOW_MODAL);
            selectorStage.initOwner(stage);
            Scene scene = new Scene(page);
            selectorStage.setScene(scene);
            SelectorsCheckerController controller = loader.getController();
            controller.setStage(selectorStage);
            selectorStage.showAndWait();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

}
