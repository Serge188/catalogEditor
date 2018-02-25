package application.model.sites;

import application.model.Model;
import application.model.ShopItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;

public class CommonStrategy {

    private Model model;
    Map<Integer, ShopItem> downloadedItems = new HashMap<>();



    public void setModel(Model model){
        this.model = model;
    }

    //Метод для обработки всех страниц сайта и преобразования их (если возможно) в объекты ShopItem с помощью
    //метода getSingleItem().
    public Map<Integer, ShopItem> getItems(String url){
        String siteName = url.split("/")[2];
        try{
            Document document = Jsoup.connect(url).get();
            ShopItem rootShopItem = getSingleItem(document);
            rootShopItem.setId(1);
            rootShopItem.setParent(0);
            rootShopItem.setUrl(url);
            buildItemMap(rootShopItem, siteName);
        } catch (IOException e){
            e.printStackTrace();
        }
        return downloadedItems;
    }

    int count = 2;
    List<String> usedLinks = new ArrayList<>();


    //Обходим страницы сайта и пытаемся построить дерево.
    public void buildItemMap(ShopItem parentShopItem, String siteName) throws IOException {
        String parentUrl = parentShopItem.getUrl();
        if(!parentUrl.endsWith("/")){
            parentShopItem.setUrl(parentUrl + "/");
        }
        usedLinks.add(parentShopItem.getUrl());
        downloadedItems.put(parentShopItem.getId(), parentShopItem);
        if (!parentShopItem.isItem()) {
            try {
                Document doc = Jsoup.connect(parentShopItem.getUrl()).get();
                List<ShopItem> currentObservableItems = new ArrayList<>();
                for (Element el : doc.select("a")) {
                    String currentLink = el.attr("abs:href");
                    if (currentLink.contains("http") &&
                            !usedLinks.contains(currentLink) &&
                            !currentLink.equals(parentShopItem.getUrl()) &&
                            currentLink.contains(siteName) &&
                            !currentLink.contains("#") &&
                            !currentLink.contains("/?") &&
                            !currentLink.contains("/&") &&
                            !currentLink.contains(".php") //&&
                            //currentLink.split("/").length <= 6
                            ) {
                        System.out.println(currentLink);
                        System.out.println(count);
                        usedLinks.add(currentLink);
                        Document currentDoc = null;
                        ShopItem currentItem = new ShopItem();
                        try {
                            currentDoc = Jsoup.connect(currentLink).get();
                            currentItem = getSingleItem(currentDoc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        currentItem.setId(count);
                        count++;
                        currentItem.setParent(parentShopItem.getId());
                        currentItem.setUrl(currentLink);
                        currentObservableItems.add(currentItem);
                    }
                }
                for (ShopItem si : currentObservableItems) {
                    buildItemMap(si, siteName);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    //Метод для извлечения данных со страницы товара и преобразования их в объект ShopItem.
    public ShopItem getSingleItem(Document doc){
        ShopItem shopItem = new ShopItem();
        Element titleElement = null;
        try {
            titleElement = doc.selectFirst(model.getProperty("testPageTitle"));
        } catch(Exception e){
            e.printStackTrace();
        }
        if(titleElement != null){
            shopItem.setPageTitle(titleElement.text());
        } else {
            shopItem.setPageTitle(doc.location());
        }
        Element priceElement = doc.selectFirst(model.getProperty("testPrice"));
        if(priceElement != null){
            String priceText = priceElement.text().replaceAll("\\D", "");
            int price = 0;
            try{
                int priceNative = Integer.parseInt(priceText);
                double priceIndex = Double.parseDouble(model.getProperty("priceIndex"));
                price = (int)(priceNative*priceIndex);
            } catch (NumberFormatException e){
                System.out.println("Цена не определена");
            }
            shopItem.setPrice(price);
        } else {
            shopItem.setPrice(0);
        }
        Element introElement = doc.selectFirst(model.getProperty("testIntro"));
        if(introElement != null){
            shopItem.setIntrotext(introElement.text());
        } else {
            shopItem.setIntrotext("");
        }
        Element introElement2 = null;
        try{
            introElement2 = doc.selectFirst(model.getProperty("testIntro2"));
        } catch (Exception e){
            e.printStackTrace();
        }
        if(introElement2 != null){
            shopItem.setIntrotext(shopItem.getIntrotext() + " " + introElement2.text());
        }
        Element contentElement = doc.selectFirst(model.getProperty("testContent"));
        if(contentElement != null){
            shopItem.setContent(contentElement.html());
        } else {
            shopItem.setContent("");
        }
        Element elementContent2 = null;
        try{
            elementContent2 = doc.selectFirst(model.getProperty("testContent2"));
        } catch(Exception e){
            e.printStackTrace();
        }
        if(elementContent2 != null){
            shopItem.setContent(shopItem.getContent() + elementContent2.html());
        }
        Element elementContent3 = null;
        try{
            elementContent3 = doc.selectFirst(model.getProperty("testContent3"));
        } catch(Exception e){
            e.printStackTrace();
        }
        if(elementContent3 != null){
            shopItem.setContent(shopItem.getContent() + elementContent3.html());
        }
        Element introElement3 = null;
        try{
            introElement3 = doc.selectFirst(model.getProperty("testIntro3"));
        } catch (Exception e){
            e.printStackTrace();
        }
        if(introElement3 != null){
            shopItem.setIntrotext(shopItem.getIntrotext() + " " + introElement3.text());
        }
        Element imageElement = doc.selectFirst(model.getProperty("testImageLink"));
        if(imageElement != null){
            String imageLink = imageElement.attr("abs:href");
            if(imageLink == null || imageLink.isEmpty()){
                imageLink = imageElement.attr("abs:src");
            }
            shopItem.setImageLink(imageLink);
        } else {
            shopItem.setImageLink("");
        }
        if(!model.getProperty("testGalleryItem").isEmpty()) {
            Elements galleryElements = doc.select(model.getProperty("testGalleryItem"));
            if (galleryElements != null && !galleryElements.isEmpty()) {
                for (Element el : galleryElements) {
                    String galleryItemLink = el.attr("abs:href");
                    if(galleryItemLink == null || galleryItemLink.isEmpty()) {
                        galleryItemLink = el.attr("abs:src");
                    }
                    shopItem.addGalleryItem(galleryItemLink);
                }
            }
        }
        String keywords = doc.getElementsByAttributeValue("name", "keywords").attr("content");
        shopItem.setUrl(doc.location());
        shopItem.setKeywords(keywords);
        shopItem.setId(1);
        shopItem.setParent(0);
        return shopItem;
    }

}
