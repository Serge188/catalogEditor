package application.model.dao;
import application.model.ShopItem;

import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ShopItemDaoImpl implements ShopItemDao {
    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;

    private final static String URL = "jdbc:mysql://176.57.210.40:3306/esytov_mpk?useUnicode=true&useSSL=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private String login;
    private String password;

    private ShopItemDaoImpl(){}

    public ShopItemDaoImpl(String login, String password){
        this.login = login;
        this.password = password;
    }

    public boolean testConnection(){
        boolean connected = setConnection();
        closeConnection();
        return connected;
    }

    //Устанавливаем соединение с базой данных
    private boolean setConnection(){
        try {
            connection = DriverManager.getConnection(URL, login, password);
            statement = connection.createStatement();
            return true;
        } catch (SQLException e){
            System.out.println("Не удалось установить соединение с базой данных.");
            return false;
        }
    }

    //Выполняем запрос к базе данных
    private void execQuery(String query){
        try{
            statement.execute(query);
            resultSet = statement.getResultSet();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    //Закрываем соединение с базой данных
    private void closeConnection(){
        if(resultSet != null){
            try {
                resultSet.close();
            } catch (SQLException e){}
            resultSet = null;
        }
        if (statement != null){
            try{
                statement.close();
            } catch (SQLException e){}
            statement = null;
        }
        if(connection != null) {
            try {
                connection.close();
            } catch(SQLException e){}
            connection = null;
        }
    }

    //Вытягиваем все товары из каталога на сайте. Метод вызывается автоматически при запуске приложения
    public Map<Integer, ShopItem> getAllItems() {
        Map<Integer, ShopItem> itemsMap = new HashMap<Integer, ShopItem>();
        Map<Integer, Integer> pricesMap = new HashMap<Integer, Integer>();
        Map<Integer, String> imagesMap = new HashMap<>();
        Map<Integer, String> galleriesMap = new HashMap<>();
        setConnection();

        execQuery("SELECT * FROM `modx_shopmodx_products`");
        if (resultSet == null) return null;
        try {
            while (resultSet.next()) {
                pricesMap.put(resultSet.getInt("resource_id"), resultSet.getInt("sm_price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        execQuery("SELECT * FROM `modx_site_tmplvar_contentvalues`");
        try{
            if(resultSet != null) {
                int tvID;
                while (resultSet.next()) {
                    tvID = resultSet.getInt("tmplvarid");
                    if(tvID == 7) {
                        String value = resultSet.getString("value");
                        if(value.contains("http://mebelpokarmany.ru/assets/images/")){
                            imagesMap.put(resultSet.getInt("contentid"), value);
                        } else {
                            imagesMap.put(resultSet.getInt("contentid"), "http://mebelpokarmany.ru/assets/images/" + value);
                        }
                    }
                    if (tvID == 10){
                        galleriesMap.put(resultSet.getInt("contentid"), resultSet.getString("value"));
                    }
                }
            }
        } catch(SQLException e){
            e.printStackTrace();
        }

        execQuery("SELECT id, pagetitle, longtitle, description, alias," +
                "published, parent, isfolder, introtext, content FROM modx_site_content");

        try{
            while (resultSet.next()) {
                ShopItem shopItem = new ShopItem();
                int id = resultSet.getInt("id");
                shopItem.setId(id);
                shopItem.setPageTitle(resultSet.getString("pagetitle"));
                String longTitle = resultSet.getString("longtitle");
                if (longTitle != null && longTitle.length() != 0) shopItem.setLongTitle(longTitle);
                String description = resultSet.getString("description");
                if (description != null && description.length() != 0) shopItem.setDescription(description);
                String alias = resultSet.getString("alias");
                if (alias != null && alias.length() != 0) shopItem.setAlias(alias);
                shopItem.setPublished(resultSet.getInt("published"));
                shopItem.setParent(resultSet.getInt("parent"));
                shopItem.setFolder(resultSet.getInt("isfolder"));
                String introtext = resultSet.getString("introtext");
                if (introtext != null && introtext.length() != 0) {
                    shopItem.setIntrotext(introtext);
                }
                String content = resultSet.getString("content");
                if (content != null && content.length() != 0) {
                    shopItem.setContent(content);
                }
                if (pricesMap.containsKey(id)) {
                    shopItem.setPrice(pricesMap.get(id));
                }
                if(imagesMap.containsKey(id)){
                    shopItem.setImageLink(imagesMap.get(id));
                }
                if(galleriesMap.containsKey(id)){
                    String migx = galleriesMap.get(id);
                    String[] migxItems = migx.split(",");
                    for(String s : migxItems){
                        if(s.contains("image")){
                            //String galleryItemUrl = s.replaceAll("\"", "").replaceAll("image", "").replaceAll(":", "").replaceAll("}", "").replaceAll("/\\/", "");
                            String galleryItemUrl = s.replaceAll("[\":}\\\\]", "").replaceAll("image", "");
                            shopItem.addGalleryItem("http://mebelpokarmany.ru/assets/images/" + galleryItemUrl);
                        }
                    }

                }
                itemsMap.put(id, shopItem);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        closeConnection();
        return itemsMap;
    }

    //Получаем товар из базы данных по ID. (Не помню, чтобы метод где-то вызывался, но интуиция подсказывает, что он не лишний)
    public ShopItem getItemById(int id){
        ShopItem shopItem = new ShopItem();
        setConnection();
        execQuery("SELECT pagetitle, longtitle, description, alias," +
                "published, parent, isfolder, introtext, content FROM modx_site_content WHERE id=" + id);
        if (resultSet == null) return null;
        try {
            shopItem.setPageTitle(resultSet.getString("pagetitle"));
            String longTitle = resultSet.getString("longtitle");
            if (longTitle != null && longTitle.length() != 0) shopItem.setLongTitle(longTitle);
            String description = resultSet.getString("description");
            if (description != null && description.length() != 0) shopItem.setLongTitle(description);
            String alias = resultSet.getString("alias");
            if (alias != null && alias.length() != 0) shopItem.setLongTitle(alias);
            shopItem.setPublished(resultSet.getInt("published"));
            shopItem.setParent(resultSet.getInt("parent"));
            shopItem.setFolder(resultSet.getInt("isfolder"));
            String introtext = resultSet.getString("introtext");
            if (introtext != null && introtext.length() != 0) shopItem.setLongTitle(introtext);
            String content = resultSet.getString("content");
            if (content != null && content.length() != 0) shopItem.setLongTitle(content);

        } catch(SQLException e){
            e.printStackTrace();
        }
        execQuery("SELECT sm_price FROM modx_shopmodx_products WHERE resource_id=" + id);
        try{
            if (resultSet != null){
                shopItem.setPrice(resultSet.getInt("sm_price"));
            }
        } catch(SQLException e){

        }
        execQuery("SELECT value FROM modx_site_tmplvar_contentvalues WHERE contentid=" + id + " AND tmplvarid=7");
        try {
            if (resultSet != null) {
                shopItem.setImageLink("http://mebelpokarmany.ru/assets/images/" + resultSet.getString("value"));
            }
        } catch (SQLException e){}

        closeConnection();
        return shopItem;
    }

    //Внесение в базу информации о вновь созданном товаре
    public void createItem(ShopItem shopItem) {
        setConnection();
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `modx_site_content` (`id`, `type`, `contentType`, `pagetitle`, `longtitle`, `description`, `alias`,");
        sb.append(" `link_attributes`, `published`, `pub_date`, `unpub_date`, `parent`, `isfolder`, `introtext`, `content`,");
        sb.append(" `richtext`, `template`, `menuindex`, `searchable`, `cacheable`, `createdby`, `createdon`, `editedby`, `editedon`,");
        sb.append(" `deleted`, `deletedon`, `deletedby`, `publishedon`, `publishedby`, `menutitle`, `donthit`, `privateweb`,");
        sb.append(" `privatemgr`, `content_dispo`, `hidemenu`, `class_key`, `context_key`, `content_type`, `uri`, `uri_override`,");
        sb.append(" `hide_children_in_tree`, `show_in_tree`, `properties`)");
        sb.append(" VALUES (");
        sb.append("'" + shopItem.getId() + "'");
        sb.append(", 'document', 'text/html'");
        sb.append(", '" + shopItem.getPageTitle() + "'");
        sb.append(", '" + shopItem.getLongTitle() + "'");
        sb.append(", '" + shopItem.getDescription() + "'");
        sb.append(", '" + shopItem.getAlias() + "'");
        sb.append(", ''");
        sb.append(", '" + shopItem.isPublished() + "'");
        sb.append(", '0', '0'");
        sb.append(", '" + shopItem.getParent() + "'");
        sb.append(", '" + shopItem.isFolder() + "'");
        sb.append(", '" + shopItem.getIntrotext() + "'");
        sb.append(", '" + shopItem.getContent() + "'");
        sb.append(", '1', '3', '0', '1', '1', '36', '" + (int)(new Date().getTime()/1000) + "', '36', '" + (int)(new Date().getTime()/1000) + "'");
        sb.append(", '0', '0', '0', '" + (int)(new Date().getTime()/1000) + "', '36', '', '0', '0', '0', '0', '0', 'ShopmodxResourceProduct', 'web', '1', '" + shopItem.getAlias() + "', '0', '0', '1', NULL)");
        String query = sb.toString();
        execQuery(query);

        query = String.format("INSERT INTO `modx_shopmodx_products` (`id`, `resource_id`, `class_key`, `sm_price`, `sm_currency`, `sm_article`) VALUES (NULL, '%d', 'ShopmodxProduct', '%d', 79, NULL);", shopItem.getId(), shopItem.getPrice());
        execQuery(query);

        if(shopItem.getImageLink() != null) {
            if(shopItem.getImageLink().contains("http://mebelpokarmany.ru")){
                query = String.format("INSERT INTO `modx_site_tmplvar_contentvalues` (`id`, `tmplvarid`, `contentid`, `value`) VALUES (NULL, '7', '%d', '%s')", shopItem.getId(), shopItem.getImageLink().replaceAll("http://mebelpokarmany.ru/assets/images/", ""));
            } else {
                query = String.format("INSERT INTO `modx_site_tmplvar_contentvalues` (`id`, `tmplvarid`, `contentid`, `value`) VALUES (NULL, '7', '%d', '%s')", shopItem.getId(), shopItem.getImageLink());
            }
            execQuery(query);
        }
        if(shopItem.getGallery() != null && shopItem.getGallery().size() > 0) {
            StringBuilder sbGallery = new StringBuilder();
            sbGallery.append("[");
            int count = 1;
            for(String link: shopItem.getGallery()) {
                sbGallery.append("{\"MIGX_id\":\"" + count + "\",\"title\":\"\",\"image\":\"");
                String cutLink = link.replaceAll("http://mebelpokarmany.ru/assets/images/", "");
                sbGallery.append(cutLink);
                sbGallery.append("\"},");
                count++;

            }
            String value = sbGallery.toString().replaceAll("},$", "}]");
            query = String.format("INSERT INTO `modx_site_tmplvar_contentvalues` (`id`, `tmplvarid`, `contentid`, `value`) VALUES (NULL, '10', '%d', '%s')", shopItem.getId(), value);
            execQuery(query);
        }
        if(shopItem.getKeywords() != null) {
            query = String.format("INSERT INTO `modx_site_tmplvar_contentvalues` (`id`, `tmplvarid`, `contentid`, `value`) VALUES (NULL, '9', '%d', '%s')", shopItem.getId(), shopItem.getKeywords());
            execQuery(query);
        }
        if(shopItem.getUrl() != null && !shopItem.getUrl().isEmpty()) {
            query = String.format("INSERT INTO `modx_site_tmplvar_contentvalues` (`id`, `tmplvarid`, `contentid`, `value`) VALUES (NULL, '11', '%d', '%s')", shopItem.getId(), shopItem.getUrl());
            execQuery(query);
        }
        closeConnection();
    }

    //Изменение существующего товара в базе данных
    public void updateItem(ShopItem shopItem) {
        int id = shopItem.getId();
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE `modx_site_content` SET ");
        sb.append("pagetitle='" + shopItem.getPageTitle() + "'");
        sb.append(", longtitle='" + shopItem.getLongTitle() + "'");
        sb.append(", description='" + shopItem.getDescription() + "'");
        sb.append(", alias='" + shopItem.getAlias() + "'");
        sb.append(", published='" + shopItem.isPublished() + "'");
        sb.append(", parent='" + shopItem.getParent() + "'");
        sb.append(", isfolder='" + shopItem.isFolder() + "'");
        sb.append(", introtext='" + shopItem.getIntrotext() + "'");
        sb.append(", content='" + shopItem.getContent() + "'");
        sb.append(" WHERE id='" + id + "'");

        setConnection();
        String query = sb.toString();
        execQuery(query);

        query = "UPDATE `modx_shopmodx_products` SET `sm_price`='" + shopItem.getPrice() + "' WHERE resource_id='" + id + "'";
        execQuery(query);

        if(shopItem.getImageLink() != null && shopItem.getImageLink().length() > 0) {
            query = "UPDATE `modx_site_tmplvar_contentvalues` SET value='" + shopItem.getImageLink() + "' WHERE contentid='" + id + "' AND tmplvarid=7";
            execQuery(query);
        }

        if(shopItem.getGallery().size() > 0) {
            StringBuilder sbGallery = new StringBuilder();
            sbGallery.append("[");
            int count = 1;
            for (String link : shopItem.getGallery()) {
                sbGallery.append("{\"MIGX_id\":\"" + count + "\",\"title\":\"\",\"image\":\"");
                String cutLink = link.replaceAll("http://mebelpokarmany.ru/assets/images/", "");
                sbGallery.append(cutLink);
                sbGallery.append("\"},");
                count++;
            }
            String value = sbGallery.toString().replaceAll("},$", "}]");
            query = "UPDATE `modx_site_tmplvar_contentvalues` SET value='" + value + "' WHERE contentid='" + id + "' AND tmplvarid=10";
            execQuery(query);
        }

        if(shopItem.getKeywords() != null && shopItem.getKeywords().length() > 0) {
            query = "UPDATE `modx_site_tmplvar_contentvalues` SET value='" + shopItem.getKeywords() + "' WHERE contentid='" + id + "' AND tmplvarid=9";
            execQuery(query);
        }

        if(shopItem.getUrl() != null && shopItem.getUrl().length() > 0) {
            query = "UPDATE `modx_site_tmplvar_contentvalues` SET value='" + shopItem.getUrl() + "' WHERE contentid='" + id + "' AND tmplvarid=11";
            execQuery(query);
        }

        closeConnection();
    }

    //Удаление товара из базы данных
    public void deleteItem(ShopItem shopItem) {
        int id = shopItem.getId();
        setConnection();

        String query = "DELETE FROM modx_site_content WHERE id=" + id;
        execQuery(query);

        query = "DELETE FROM modx_shopmodx_products WHERE resource_id=" + id;
        execQuery(query);

        query = "DELETE FROM `modx_site_tmplvar_contentvalues` WHERE contentid='" + id + "'";
        execQuery(query);

        closeConnection();
    }


}
