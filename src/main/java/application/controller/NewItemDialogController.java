package application.controller;

import application.model.ShopItem;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class NewItemDialogController {
    @FXML
    private Label idLabel;

    @FXML
    private TextField parentIdField;

    @FXML
    private TextField pageTitleField;

    @FXML
    private TextField longTitleField;

    @FXML
    private TextField priceField;

    @FXML
    private TextArea introTextArea;

    @FXML
    private HTMLEditor contentEditor;

    @FXML
    private ImageView mainImage;

    @FXML
    private FlowPane gallery;


    private Stage itemDialogStage;
    private ShopItem shopItem;
    private boolean okClicked = false;

    @FXML
    public void initialize(){}

    //Нажатие кнопки "Сохранить". Сохраняются параметры нового / отредактированного товара
    @FXML
    private ShopItem handleSave(){
        shopItem.setParent(Integer.parseInt(parentIdField.getText()));
        shopItem.setPageTitle(pageTitleField.getText());
        shopItem.setLongTitle(longTitleField.getText());
        shopItem.setPrice(Integer.parseInt(priceField.getText()));
        shopItem.setIntrotext(introTextArea.getText());
        shopItem.setContent(contentEditor.getHtmlText());
        okClicked = true;
        itemDialogStage.close();
        return shopItem;
    }

    @FXML
    private void handleCancel(){
        itemDialogStage.close();
    }

    //Серия методов, задействованных в перетаскивании изображений в форму.
    @FXML
    private void handleDragOver(DragEvent event){
        if(event.getDragboard().hasFiles()){
            event.acceptTransferModes(TransferMode.ANY);
        }
    }


    @FXML
    private void handleImageDrop(DragEvent event) throws FileNotFoundException {
        List<File> list = event.getDragboard().getFiles();
        Image image = new Image(new FileInputStream(list.get(0)));
        mainImage.setImage(image);
        String imageLink = list.get(0).getPath().replaceAll("\\\\", "/");
        shopItem.setImageLink(imageLink);
    }

    @FXML
    private void handleGalleryDrop(DragEvent event) throws FileNotFoundException{
        List<File> list = event.getDragboard().getFiles();
        for(File f : list){
            Image image = new Image(new FileInputStream(f));
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            gallery.getChildren().add(imageView);
            String imageLink = f.getPath().replaceAll("\\\\", "/");
            shopItem.addGalleryItem(imageLink);
        }
    }

    @FXML
    private void handleImageMouseClick(MouseEvent event){
        if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() == 2){
                mainImage.setImage(null);
            }
        }
    }

    //Удаляем изображения из формы при двойном клике
    @FXML
    private void handleGalleryMouseClick(MouseEvent event){
        if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() == 2){
                gallery.getChildren().setAll();
            }
        }
    }


    public void setNewItemDialogStage(Stage stage){
        this.itemDialogStage = stage;
    }

    public void setShopItem(ShopItem shopItem) {
        this.shopItem = shopItem;
        if(shopItem != null){
            gallery.getChildren().setAll();
            if(shopItem.getImageLink() != null && shopItem.getImageLink().length() > 0) {
                Image image = new Image(shopItem.getImageLink(), true);
                mainImage.setImage(image);
            }
            if (shopItem.getGallery() != null && shopItem.getGallery().size() > 0){
                Image image = null;
                ImageView imageView = null;
                String galleryItemLink = null;
                for(int i = 0; i < shopItem.getGallery().size(); i++){
                    if(shopItem.getGalleryItem(i).endsWith("]")){
                        galleryItemLink = shopItem.getGalleryItem(i).replaceAll("]$", "");
                    } else {
                        galleryItemLink = shopItem.getGalleryItem(i);
                    }
                    try {
                        image = new Image(galleryItemLink, true);
                        imageView = new ImageView(image);
                        imageView.setFitHeight(50);
                        imageView.setFitWidth(50);
                        //String s = shopItem.getGalleryItem(i);
                        gallery.getChildren().add(imageView);
                    } catch(IllegalArgumentException e){
                        e.printStackTrace();
                    }
                }
            }
            idLabel.setText(String.valueOf(shopItem.getId()));
            parentIdField.setText(String.valueOf(shopItem.getParent()));
            pageTitleField.setText(shopItem.getPageTitle());
            longTitleField.setText(shopItem.getLongTitle());
            priceField.setText(String.valueOf(shopItem.getPrice()));
            introTextArea.setText(shopItem.getIntrotext());
            contentEditor.setHtmlText(shopItem.getContent());
        } else {
            mainImage.setImage(null);
            idLabel.setText("");
            parentIdField.setText("");
            pageTitleField.setText("");
            longTitleField.setText("");
            priceField.setText("");
            introTextArea.setText("");
            contentEditor.setHtmlText("");
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private boolean isInputValid(){
        String errorMessage = "";
        String s = pageTitleField.getText();
        if(pageTitleField.getText() == null || pageTitleField.getText().length() == 0) {
            errorMessage += "Заголовок страницы";
        }
        if(introTextArea.getText() == null || introTextArea.getText().length() == 0) {
            errorMessage += " Описание ";
        }
        if(contentEditor.getHtmlText() == null || contentEditor.getHtmlText().length() == 0) {
            errorMessage += " Содержание ";
        }
        if(priceField.getText() == null || priceField.getText().length() == 0) {
            errorMessage += "Цена";
        } else {
            try{
                Integer.parseInt(priceField.getText());
            } catch (NumberFormatException e){
                errorMessage += "Price";
            }
        }

        if(errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input error");
            alert.setHeaderText("Please check input data");
            alert.setContentText(errorMessage);

            alert.showAndWait();
            return false;
        }
    }
}
