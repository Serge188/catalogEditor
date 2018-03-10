package application.controller;

import application.TreeApp;
import application.model.Model;
import application.model.ShopItem;
import application.model.sites.SiteParser;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;

import java.util.*;

public class TreeOverviewController {
    @FXML
    private ImageView mainImage;

    @FXML
    private TreeTableView<ShopItem> treeTableView;

    @FXML
    private TreeTableColumn<ShopItem, Integer> idColumn;

    @FXML
    private TreeTableColumn<ShopItem, String> titleColumn;

    @FXML
    private TreeTableColumn<ShopItem, Integer> priceColumn;

    @FXML
    private TreeTableColumn<ShopItem, ImageView> imageColumn;

    @FXML
    private TextField pageTitleField;

    @FXML
    private TextField priceField;

    @FXML
    private TextArea introTextArea;

    @FXML
    private HTMLEditor contentEditor;

    @FXML
    private HBox gallery;

    @FXML
    private Button refreshButton;

    @FXML
    private Button createButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private Button moveButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button addNewSite;

    @FXML
    private Button testButton;

    @FXML
    private Label currentPageLabel;

    @FXML
    TreeTableView<ShopItem> treeTableRight;

    @FXML
    private TreeTableColumn<ShopItem, Integer> idColumnRight;

    @FXML
    private TreeTableColumn<ShopItem, String> titleColumnRight;

    @FXML
    private TreeTableColumn<ShopItem, String> movedColumnRight;

    @FXML
    private TreeTableColumn<ShopItem, Integer> priceColumnRight;

    @FXML
    private TreeTableColumn<ShopItem, ImageView> imageColumnRight;

    private TreeApp treeApp;
    private Model model;
    private SiteParser parser;

    @FXML
    private void initialize(){
        idColumn.setCellValueFactory(cellData -> cellData.getValue().getValue().getIdProperty().asObject());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().getValue().getPageTitleProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().getValue().getPriceProperty().asObject());
        imageColumn.setCellValueFactory(cellData -> cellData.getValue().getValue().getMainImageforTable());

        treeTableView.setShowRoot(true);
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        idColumnRight.setCellValueFactory(cellData -> cellData.getValue().getValue().getIdProperty().asObject());
        titleColumnRight.setCellValueFactory(cellData -> cellData.getValue().getValue().getPageTitleProperty());
        movedColumnRight.setCellValueFactory(cellData -> cellData.getValue().getValue().isMovedProperty());
        priceColumnRight.setCellValueFactory(cellData -> cellData.getValue().getValue().getPriceProperty().asObject());
        imageColumnRight.setCellValueFactory(cellData -> cellData.getValue().getValue().getMainImageforTable());

        treeTableRight.setShowRoot(true);
        treeTableRight.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    //Нажатие кнопки "Обновить"
    @FXML
    private void handleRefresh(){
        treeTableView.getRoot().getChildren().clear();
        treeTableView.setRoot(null);
        showCatalog();
    }

    //Нажатие кнопки "Создать"
    @FXML
    private void handleCreate(){
        ShopItem shopItem = new ShopItem();
        TreeItem selectedItem = treeTableView.getSelectionModel().getSelectedItem();
        boolean ok = treeApp.showNewItemDialog(shopItem);
        if(ok) {

            if(selectedItem != null){
                ShopItem selectedShopItem = (ShopItem) selectedItem.getValue();
                shopItem.setParent(selectedShopItem.getId());
                selectedItem.getChildren().add(new TreeItem<ShopItem>(shopItem));
            } else {
                treeTableView.getRoot().getChildren().add(new TreeItem<ShopItem>(shopItem));
            }
            model.createItem(shopItem);
        }
    }

    //Нажатие кнопки "Редактировать". Копируем выделенный товар, редактируем копию, передаем модели "старую" и "новую" версию товара.
    @FXML
    private void handleEdit(){
        ShopItem shopItem = treeTableView.getSelectionModel().getSelectedItem().getValue();
        ShopItem oldVersionItem = ShopItem.cloneShopItem(shopItem);
        boolean ok = treeApp.showNewItemDialog(shopItem);
        if(ok) {
            model.updateItem(oldVersionItem, shopItem);
        }
    }

    //Нажатие кнопки "Удалить"
    @FXML
    private void handleDelete(){
        TreeItem selectedItem = treeTableView.getSelectionModel().getSelectedItem();
        if(selectedItem != null) {
            ShopItem selectedShopItem = (ShopItem) selectedItem.getValue();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Удаление элемента");
            alert.setHeaderText("Вы действительно хотите удалить элемент \"" + selectedShopItem.getPageTitle() + "\"");
            alert.setContentText("Нажмите ОК для подтверждения.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                TreeItem parentItem = selectedItem.getParent();
                Stack<TreeItem> stack = new Stack<>();
                stack.push(selectedItem);
                while(!stack.isEmpty()) {
                    TreeItem<ShopItem> currentTreeItem = stack.pop();
                    TreeItem<ShopItem> currentParent = currentTreeItem.getParent();
                    if(currentTreeItem.getChildren() != null && currentTreeItem.getChildren().size() > 0){
                        for(Object o : currentTreeItem.getChildren()){
                            TreeItem<ShopItem> ti = (TreeItem) o;
                            stack.push(ti);
                        }
                    }
                    ShopItem currentShopItem = (ShopItem) currentTreeItem.getValue();
                    model.deleteItem(currentShopItem);
                    currentParent.getChildren().remove(selectedItem);
                }
            }
        }
    }

    //Нажатие кнопки "Добавить сайт". Открывается окно для формирования запросов к стороннему сайту
    @FXML
    private void handleAddNewSite(){
        boolean ok = treeApp.showNewSiteDialog();
        if(ok){
            Service<Void> downloadingItems = new Service<Void>(){
                @Override
                protected Task<Void> createTask(){
                  return new Task<Void>(){
                    @Override
                    protected Void call(){
                        setRightButtonsDisable(true);
                        Map<Integer, ShopItem> map = parser.downloadItems();
                        setConnectionsToParents(map);
                        ShopItem rootShopItem = map.get(1);
                        TreeItem<ShopItem> rootNode = new TreeItem<>(rootShopItem);
                        treeTableRight.setRoot(rootNode);
                        buildTree(rootNode, map);
                        return null;
                    }
                  };
                }
            };

            downloadingItems.setOnSucceeded((event)->{
                setRightButtonsDisable(false);
                currentPageLabel.setText("");
            });
            downloadingItems.restart();

        }
    }

    @FXML
    private void handleTestSite(){
        boolean ok = treeApp.showNewSiteDialog();
        if(ok){
            ShopItem testShopItem = parser.getTestItem();
            TreeItem<ShopItem> rootNode = new TreeItem<>(testShopItem);
            treeTableRight.setRoot(rootNode);
        }
    }

    //Нажатие кнопки "Перенести". Копируются выделенные товары из правой таблицы в левую.
    @FXML
    private void handleMove(){
        Service<Void> movingItems = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    public Void call() {
                        setRightButtonsDisable(true);
                        ShopItem shopItemToMove;
                        TreeItem<ShopItem> selectedTreeItem = treeTableView.getSelectionModel().getSelectedItem();
                        int parentId = selectedTreeItem.getValue().getId();
                        ObservableList<TreeItem<ShopItem>> selectedList = treeTableRight.getSelectionModel().getSelectedItems();
                        for (TreeItem ti : selectedList) {
                            shopItemToMove = (ShopItem) ti.getValue();
                            String currentPageTitle = shopItemToMove.getPageTitle();
                            Platform.runLater(() -> currentPageLabel.setText("Копирование " + currentPageTitle));
                            shopItemToMove.setParent(parentId);
                            shopItemToMove.setMoved("v");
                            selectedTreeItem.getChildren().add(new TreeItem<>(shopItemToMove));
                            model.createItem(shopItemToMove);
                        }
                        return null;
                    }
                };
            }
        };
        movingItems.setOnSucceeded((event) -> {
            setRightButtonsDisable(false);
            currentPageLabel.setText("");
        });
        movingItems.restart();
    }

    //Обработка двойного нажатия ЛКМ на левой таблице. Открывается окно редактирования выделенного товара.
    @FXML
    public void handleTableMouseClick(MouseEvent event){
        if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() == 2){
                handleEdit();
            }
        }
    }

    //Обработка двойного нажатия ЛКМ на левой таблице. Открывается окно редактирования выделенного товара.
    @FXML
    public void handleRightTableMouse(MouseEvent event){
        if(event.getButton().equals(MouseButton.PRIMARY)){
            if(event.getClickCount() == 2){
                ShopItem shopItem = treeTableRight.getSelectionModel().getSelectedItem().getValue();
                treeApp.showNewItemDialog(shopItem);
            }
        }
    }

    public TreeOverviewController() {
    }

    public void setTreeApp(TreeApp treeApp){
        this.treeApp = treeApp;
        model = treeApp.getModel();
        parser = treeApp.getParser();
        parser.setController(this);
        showCatalog();
    }

    private void showCatalog(){
        Map<Integer, ShopItem> catalogItems = model.getAllItems();
        setConnectionsToParents(catalogItems);
        TreeItem<ShopItem> rootNode = new TreeItem<>(catalogItems.get(85));
        treeTableView.setRoot(rootNode);
        buildTree(rootNode, catalogItems);
    }

    //Рекурсивное построение дерева товаров (Для основного каталога и для товаров со стороннего сайта).
    private void buildTree(TreeItem<ShopItem> parentTreeItem, Map<Integer, ShopItem> map){
        ShopItem parentShopItem = parentTreeItem.getValue();
        if(parentShopItem != null && parentShopItem.getChildrenIds() != null && parentShopItem.getChildrenIds().size() != 0){
            for(int i : parentShopItem.getChildrenIds()){
                ShopItem currentShopItem = map.get(i);
                TreeItem<ShopItem> currentTreeItem = new TreeItem<>(currentShopItem);
                parentTreeItem.getChildren().add(currentTreeItem);
                buildTree(currentTreeItem, map);
            }
        }
    }

    //Вспомогательный метод для установления связей между родительскими и дочерними элементами. (Для построения дерева).
    private void setConnectionsToParents(Map<Integer, ShopItem> map){
        Map<Integer, ShopItem> clonedMap = new HashMap<>(map);
        for(Map.Entry<Integer, ShopItem> pair : clonedMap.entrySet()){
            int currentId = pair.getValue().getId();
            int parentId = pair.getValue().getParent();
            ShopItem parentShopItem = map.get(parentId);
            if(parentShopItem != null) {
                parentShopItem.addChildId(currentId);
            }
        }
    }

    public void updateLabel(String text){
        currentPageLabel.setText(text);
    }

    private void setRightButtonsDisable(boolean b){
        moveButton.setDisable(b);
        testButton.setDisable(b);
        addNewSite.setDisable(b);
    }
}
