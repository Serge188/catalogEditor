package application.model;

import application.consoleHelper.Transliterator;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

//Класс товара - основной сущности каталога
public class ShopItem {
    private IntegerProperty id; //Порядковый номер в базе данных
    private StringProperty pageTitle; //Название
    private StringProperty longTitle; //Расширенное название, отображается при открытии товара
    private StringProperty description; //Краткое описание
    private StringProperty alias; //Псевдоним, необходим для создания уникального URL товара (не забыть придумать метод для формирования псевдонима из названия товара)
    private IntegerProperty isPublished; //Опубликована\Не опубликована страница товара на сайте (по сути тип boolean, но в БД принимает int значения 1\0)
    private IntegerProperty parent; //ID страницы-родителя
    private IntegerProperty isFolder; //Страница товара является\не является контейнером (по сути тип boolean, но БД принимает int значения 1\0)
    private StringProperty introtext; //Аннотация
    private StringProperty content; //Основное описание товара
    private IntegerProperty price; //Цена
    private List<Integer> childrenIds; //Список ID дочерних элементов
    private StringProperty url; //Нативная ссылка на товар, на исходном сайте
    private List<String> childrenUrls; //Список ссылок дочерних товаров
    private String imageLink; //Ссылка на основное изображения товара
    private List<String> gallery; //Список ссылок на дополнительные изображения
    private StringProperty moved; //Метка о копировании товара в основной каталог
    private String keywords; //Ключевые слова

    //Конструктор
    public ShopItem() {
        this.id = new SimpleIntegerProperty(0);
        this.pageTitle = new SimpleStringProperty("");
        this.longTitle = new SimpleStringProperty("");
        this.description = new SimpleStringProperty("");
        this.alias = new SimpleStringProperty("");
        this.isPublished = new SimpleIntegerProperty(1);
        this.parent = new SimpleIntegerProperty(85);
        this.isFolder = new SimpleIntegerProperty(0);
        this.introtext = new SimpleStringProperty("");
        this.content = new SimpleStringProperty("");
        this.price = new SimpleIntegerProperty();
        this.childrenIds = new ArrayList<Integer>();
        this.childrenUrls = new ArrayList<String>();
        this.url = new SimpleStringProperty("");
        this.imageLink = "";
        this.gallery = new ArrayList<String>();
        this.moved = new SimpleStringProperty("");
        this.keywords = "";
    }

    //Метод для создания копии товара. Перед редактированием товар будет "клонироваться",
    //затем в соотвествующий метод будет передаваться "новая" и "старая" версии.
    public static ShopItem cloneShopItem(ShopItem itemToClone){
        ShopItem clonedItem = new ShopItem();
        int clonedId = itemToClone.getId();
        String clonedPageTitle = itemToClone.getPageTitle();
        String clonedLongTitle = itemToClone.getLongTitle();
        String clonedDescription = itemToClone.getDescription();
        String clonedAlias = itemToClone.getAlias();
        int cloneIsPublished = itemToClone.isPublished();
        int clonedParent = itemToClone.getParent();
        int cloneIsFolder = itemToClone.isFolder();
        String clonedIntrotext = itemToClone.getIntrotext();
        String clonedContent = itemToClone.getContent();
        int clonedPrice = itemToClone.getPrice();
        List<Integer> clonedChildren = new ArrayList<>(itemToClone.getChildrenIds());
        String clonedUrl = itemToClone.getUrl();
        String clonedIimageLink = itemToClone.getImageLink();
        List<String> clonedGallery = new ArrayList<>(itemToClone.getGallery());
        clonedItem.setId(clonedId);
        clonedItem.setPageTitle(clonedPageTitle);
        clonedItem.setLongTitle(clonedLongTitle);
        clonedItem.setDescription(clonedDescription);
        clonedItem.setAlias(clonedAlias);
        clonedItem.setPublished(cloneIsPublished);
        clonedItem.setParent(clonedParent);
        clonedItem.setFolder(cloneIsFolder);
        clonedItem.setIntrotext(clonedIntrotext);
        clonedItem.setContent(clonedContent);
        clonedItem.setPrice(clonedPrice);
        clonedItem.setChildrenIds(clonedChildren);
        clonedItem.setImageLink(clonedIimageLink);
        clonedItem.setGallery(clonedGallery);
        return clonedItem;
    }

    //Геттеры
    public IntegerProperty getIdProperty(){
        return id;
    }
    public int getId() {
        return id.get();
    }
    public StringProperty getPageTitleProperty(){
        return pageTitle;
    }
    public String getPageTitle() {
        return pageTitle.get();
    }
    public String getLongTitle() {
        return longTitle.get();
    }
    public String getDescription() {
        return description.get();
    }
    public String getAlias() {
        return alias.get();
    }
    public int getParent() {
        return parent.get();
    }
    public int isPublished() {
        return isPublished.get();
    }
    public int isFolder() {
        return isFolder.get();
    }
    public String getIntrotext() {
        return introtext.get();
    }
    public String getContent() {
        return content.get();
    }
    public IntegerProperty getPriceProperty(){
        return price;
    }
    public int getPrice() {
        return price.get();
    }
    public List<Integer> getChildrenIds() {
        return childrenIds;
    }
    public int getChild(int i){
        return childrenIds.get(i);
    }
    public String getChildUrl(int i){
        return childrenUrls.get(i);
    }
    public List<String> getChildrenUrls(){
        return childrenUrls;
    }
    public boolean hasChildrenUrls(){
        return !childrenUrls.isEmpty();
    }
    public boolean hasChildrenIds(){
        return !childrenIds.isEmpty();
    }
    public String getUrl() {
        return url.get();
    }
    public boolean isItem(){
        return(longTitle.get().length() > 0 ||
                description.get().length() > 0 ||
                introtext.get().length() > 0 ||
                content.get().length() > 0 ||
                price.get() > 0 ||
                childrenIds.size() > 0 ||
                imageLink.length() > 0
        );
    }
    public String getImageLink(){
        return imageLink;
    }
    public ObjectProperty<ImageView> getMainImageforTable(){
        if (imageLink != null && imageLink.length() > 0){
            Image image = new Image(imageLink, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(30);
            imageView.setFitHeight(30);
            return new SimpleObjectProperty<ImageView>(imageView);
        }
        return null;
    }
    public StringProperty isMovedProperty(){
        return moved;
    }
    public String isMoved(){
        return moved.get();
    }
    public List<String> getGallery(){
        return gallery;
    }
    public String getGalleryItem(int i){
        return gallery.get(i);
    }
    public String getKeywords() {
        return keywords;
    }

    //Сеттеры
    public void setId(int id) {
        this.id.set(id);
    }
    public void setPageTitle(String pageTitle) {
        this.pageTitle.set(pageTitle);
        this.alias.set(Transliterator.transliteration(pageTitle));
    }
    public void setLongTitle(String longTitle) {
        this.longTitle.set(longTitle);
    }
    public void setDescription(String description) {
        this.description.set(description);
    }
    public void setAlias(String alias) {
        this.alias.set(alias);
    }
    public void setPublished(int i) {
        isPublished.set(i);
    }
    public void setParent(int parent) {
        this.parent.set(parent);
    }
    public void setFolder(int i) {
        isFolder.set(i);
    }
    public void setIntrotext(String introtext) {
        this.introtext.set(introtext);
    }
    public void setContent(String content) {
        this.content.set(content);
    }
    public void setPrice(int price) {
        this.price.set(price);
    }
    public void setChildrenIds(List<Integer> childrenIds) {
        this.childrenIds = childrenIds;
    }
    public void addChildId(int i){
        childrenIds.add(i);
    }
    public void addChildUrl(String childUrl){
        childrenUrls.add(childUrl);
    }
    public void setUrl(String url){
        this.url.set(url);
    }
    public void setImageLink(String imageLink){
        this.imageLink = imageLink;
    }
    public void setGallery(List<String> gallery){
        this.gallery = gallery;
    }
    public void addGalleryItem(String s){
        gallery.add(s);
    }
    public void clearGallery(){
        this.gallery.clear();
    }
    public void setMoved(String s){
        moved.set(s);
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "ShopItem{" +
                "id=" + id +
                ", pageTitle='" + pageTitle + '\'' +
                ", parent=" + parent +
                ", price=" + price +
                ", childrenIds=" + childrenIds +
                '}';
    }
}