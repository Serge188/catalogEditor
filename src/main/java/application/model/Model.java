package application.model;

import application.model.dao.ImageTransfer;
import application.model.dao.ShopItemDaoImpl;
import application.model.sites.CommonStrategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Model {
    private Map<Integer, ShopItem> itemsMap;
    private Map<Integer, ShopItem> downloadedItems;
    private ShopItemDaoImpl dao;
    private ImageTransfer imageTransfer;
    private Properties properties;
    CommonStrategy strategy = null;

    //При создании объекта модели сразу загружаем из БД все имеющиеся на сайте товары
    public Model() {
        this.strategy = new CommonStrategy();
        this.properties = new Properties();
        strategy.setModel(this);
        this.imageTransfer = new ImageTransfer();
    }


    public Properties getProperties() {
        return properties;
    }

    public void loadProperties(FileReader reader){
        try {
            properties.load(reader);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public String getProperty(String key){
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value){
        properties.setProperty(key, value);
    }

    public void storeProperties(Writer writer, String comments){
        try {
            properties.store(writer, comments);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //Получаем наибольший ID (для создания новых товаров)
    private int getMaxId(){
        int maxId = 0;
        for(int currentId :  itemsMap.keySet()){
            if(currentId >= maxId) maxId = currentId;
        }
        return maxId;
    }

    public boolean testConnection(){
        return dao.testConnection();
    }

    public boolean setDao(String login, String password){
        ShopItemDaoImpl dao = new ShopItemDaoImpl(login, password);
        this.dao = dao;
        return dao.testConnection();
    }

    //Возвращаем карту (ID-Товар) всех загруженных из БД товаров,
    //...предварительно установив связи между родительскими и дочерними элементами, для последующего построения дерева
    public Map<Integer, ShopItem> getAllItems(){
        this.itemsMap = dao.getAllItems();
        setConnectionsToParents(itemsMap);
        return itemsMap;
    }

    public ShopItem getItemById(int id, Map<Integer, ShopItem> map){
        return map.get(id);
    }

    public Map<Integer, ShopItem> getDownloadedItems(){
        downloadedItems = downloadItems();
        return downloadedItems;
    }

    //Метод для создания нового товара:
    // 1 - Добавляем новый товар в карту
    // 2 - Загружаем изображения нового товара на сервер
    // 3 - Вносим информацию о новом товаре в БД
    public void createItem(ShopItem shopItem){
        int id = getMaxId();
        id++;
        if(shopItem.getId() != id) shopItem.setId(id);
        itemsMap.put(id, shopItem);
        imageTransfer.uploadImages(shopItem);
        dao.createItem(shopItem);
    }

    //Метод для обновления существующего товара
    //В метод передается старая и новая версии товара
    //Данные в БД обновляются. Если названия в старой и новой версии отличаются,
    //удалем директорию с изображениями и создаем новую
    //Если обрабатываемый товар не существует, вносим его, как новый.
    public void updateItem(ShopItem oldVersionItem, ShopItem newVersionItem){
        int id = newVersionItem.getId();
        if(itemsMap.containsKey(id)) {
            itemsMap.put(id, newVersionItem);
            if(!oldVersionItem.getAlias().equals(newVersionItem.getAlias())) {
                imageTransfer.uploadImages(newVersionItem);
                imageTransfer.deleteDirectory(oldVersionItem);
            } else {
                imageTransfer.updateImages(oldVersionItem, newVersionItem);
            }
            dao.updateItem(newVersionItem);
        } else {
            createItem(newVersionItem);
        }
    }

    //Удаляем товар и его директорию с изображениями
    public boolean deleteItem(ShopItem shopItem){
        int id = shopItem.getId();
        if(itemsMap.containsKey(id)) {
            itemsMap.remove(id);
            imageTransfer.deleteDirectory(shopItem);
            dao.deleteItem(shopItem);
            return true;
        } else {
            return false;
        }
    }

    public boolean isIdExists(int id){
        return itemsMap.containsKey(id);
    }

    //Получение списка ID дочерних элементов.
    public List<Integer> getChildrenIds(int id, Map<Integer, ShopItem> map){
        List<Integer> list = new ArrayList<>();
        for(Map.Entry<Integer, ShopItem> pair: map.entrySet()){
            if(pair.getValue().getParent() == id){
                list.add(pair.getKey());
            }
        }
        return list;
    }

    //Вспомогательный метод для установления связей между родительскими и дочерними элементами. (Для построения дерева).
    public void setConnectionsToParents(Map<Integer, ShopItem> map){
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

    public boolean hasChildrenIds(int id, Map<Integer, ShopItem> map){
        return !getChildrenIds(id, map).isEmpty();
    }


    //Загружаем товары со стороннего сайта, собираем их в карту (ID-Товар) и устанавливаем связи
    //между родительскими и дочернми эелементами карты.
    public Map<Integer, ShopItem> downloadItems(){
        Map<Integer, ShopItem> downloadedItems = strategy.getItems(properties.getProperty("mainUrl"));
        setConnectionsToParents(downloadedItems);
        return downloadedItems;
    }

    //Загрузка одного товара со стороннего сайта. (Используется для тестирования)
    public ShopItem getTestItem(String url){
        ShopItem shopItem = new ShopItem();
        try {
            Document document = Jsoup.connect(url).get();
            shopItem = strategy.getSingleItem(document);
            System.out.println(shopItem.isItem());
        } catch (IOException e){
            e.printStackTrace();
        }
        return shopItem;
    }

    // Аналогичная загрузка тестового набора товаров для тестирования.
    public Map<Integer, ShopItem> getTestMap(String url){
        Map<Integer, ShopItem> map = new HashMap<>();
        ShopItem shopItem = new ShopItem();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.getElementsByTag("a");
            shopItem = strategy.getSingleItem(doc);
            shopItem.setId(1);
            shopItem.setParent(0);
            map.put(1, shopItem);
            int count = 2;
            for(Element el : elements){
                String currentLink = el.attr("href");
                if(currentLink.contains("http") && currentLink.contains(url) && !currentLink.equals(url)&& !currentLink.contains("#") && !currentLink.contains("/?") && !currentLink.contains("/&") && !currentLink.contains(".php")) {
                    Document currentDoc = Jsoup.connect(currentLink).get();
                    shopItem = strategy.getSingleItem(currentDoc);
                    shopItem.setId(count);
                    shopItem.setParent(1);
                    map.put(count, shopItem);
                    System.out.println(currentLink);
                    System.out.println(count);
                    count++;
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return map;
    }

}
