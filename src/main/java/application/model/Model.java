package application.model;

import application.model.dao.ImageTransfer;
import application.model.dao.ShopItemDaoImpl;
import application.model.sites.SiteParser;
import java.util.*;

public class Model {
    private Map<Integer, ShopItem> itemsMap;
    private ShopItemDaoImpl dao;
    private ImageTransfer imageTransfer;
    private SiteParser parser = null;

    //При создании объекта модели сразу загружаем из БД все имеющиеся на сайте товары
    public Model() {
    }

    public void setParser(SiteParser parser){
        this.parser = parser;
    }

    //Получаем наибольший ID (для создания новых товаров)
    private int getMaxId(){
        int maxId = 0;
        for(int currentId :  itemsMap.keySet()){
            if(currentId >= maxId) maxId = currentId;
        }
        return maxId;
    }

    public boolean setDao(String login, String password){
        ShopItemDaoImpl dao = new ShopItemDaoImpl(login, password);
        this.dao = dao;
        return dao.testConnection();
    }

    public void setImageTransfer(String password){
        ImageTransfer imageTransfer = new ImageTransfer(password);
        this.imageTransfer = imageTransfer;
    }

    //Возвращаем карту (ID-Товар) всех загруженных из БД товаров,
    //...предварительно установив связи между родительскими и дочерними элементами, для последующего построения дерева
    public Map<Integer, ShopItem> getAllItems(){
        this.itemsMap = dao.getAllItems();
        return itemsMap;
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
}
