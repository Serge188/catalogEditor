package application.model.dao;

import application.model.ShopItem;

import java.util.Map;

public interface ShopItemDao {

    //Получаем все товары из каталога
    Map<Integer, ShopItem> getAllItems();

    //Получаем товар по ID
    ShopItem getItemById(int id);

    //Создаем новый товар
    void createItem(ShopItem shopItem);

    //Изменяем товар в каталоге
    void updateItem(ShopItem shopItem);

    //Удаляем товар
    void deleteItem(ShopItem shopItem);

}
