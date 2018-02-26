package application.model.dao;

import application.services.Transliterator;
import application.model.Model;
import application.model.ShopItem;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//Класс для работы с изображениями товаров
public class ImageTransfer {
    private final static String HOST = "176.57.210.40";
    private final static String PASSWORD = "IsjYXG83";
    private final static String SERVER_FOLDER_FOR_IMAGES = "/mebelpokarmany.ru/public_html/assets/images/products/application/";

    private Model model;
    private FTPClient fileClient;

    //Устанавливаем FTP-соедиение. Логин и пароль будут запрашиваться у пользователя.
    private void setConnection(){
        try {
            fileClient = new FTPClient();
            fileClient.connect(HOST);
            fileClient.enterLocalPassiveMode();
            fileClient.login("esytov", PASSWORD);
            fileClient.setFileType(FTP.BINARY_FILE_TYPE);
            fileClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //Закрываем FTP-соединение
    private void closeConnection(){
        try {
            fileClient.logout();
            fileClient.disconnect();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //Метод для выгрузки изображений товара на сервер
    public void uploadImages(ShopItem shopItem){
        String destinationFolder = SERVER_FOLDER_FOR_IMAGES + shopItem.getAlias();
        String sourceFilePath;
        String fileName;
        InputStream inputStream;

        setConnection();

        try{
            fileClient.makeDirectory(destinationFolder);
            System.out.println(fileClient.getReplyString());
        } catch(IOException e){
            e.printStackTrace();
            return;
        }

        if(shopItem.getImageLink() != null && shopItem.getImageLink().length() > 0) {
            sourceFilePath = shopItem.getImageLink();
            String[] folders = sourceFilePath.split("/");
            fileName = folders[folders.length - 1];
            try{
                if(!sourceFilePath.contains("http")) {
                    inputStream = new FileInputStream(sourceFilePath);
                } else {
                    URL sourceUrl = new URL(sourceFilePath);
                    inputStream = sourceUrl.openStream();
                }
                fileClient.storeFile(destinationFolder + "/" + Transliterator.transliteration(fileName), inputStream);
                System.out.println(fileClient.getReplyString());
                shopItem.setImageLink("http://mebelpokarmany.ru/assets/images/products/application/" + shopItem.getAlias() + "/" + Transliterator.transliteration(fileName));
                inputStream.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        if(shopItem.getGallery() != null && shopItem.getGallery().size() > 0){
            List<String> sourceGalleryItems = new ArrayList<>(shopItem.getGallery());
            shopItem.clearGallery();
            try {
                fileClient.makeDirectory(destinationFolder);
            } catch (IOException e){
                e.printStackTrace();
            }
            for(String galleryItem: sourceGalleryItems){
                if(galleryItem.endsWith("]")){
                    sourceFilePath = galleryItem.replaceAll("]$", "");
                } else {
                    sourceFilePath = galleryItem;
                }
                String[] folders = galleryItem.split("/");
                if(folders[folders.length - 1].endsWith("]")){
                    fileName = folders[folders.length - 1].replaceAll("]$", "");
                } else {
                    fileName = folders[folders.length - 1];
                }
                try {
                    if (!sourceFilePath.contains("http")) {
                        inputStream = new FileInputStream(sourceFilePath);
                    } else {
                        URL sourceUrl = new URL(sourceFilePath);
                        inputStream = sourceUrl.openStream();
                    }
                    fileClient.storeFile(destinationFolder + "/" + Transliterator.transliteration(fileName), inputStream);
                    shopItem.addGalleryItem("http://mebelpokarmany.ru/assets/images/products/application/" + shopItem.getAlias() + "/" + Transliterator.transliteration(fileName));
                    inputStream.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        closeConnection();
    }

    //Удаляем с сервера папку с изображениям товара
    public void deleteDirectory(ShopItem shopItem){
        setConnection();
        String destinationFolder = SERVER_FOLDER_FOR_IMAGES + shopItem.getAlias();
        try {
            FTPFile[] files = fileClient.listFiles(destinationFolder);
            if(files != null && files.length > 0){
                for (FTPFile f : files) {
                    fileClient.deleteFile(destinationFolder + "/" + f.getName());
                    System.out.println(fileClient.getReplyString());
                }
            }
            fileClient.removeDirectory(destinationFolder);
            System.out.println(fileClient.getReplyString());
        } catch (IOException e){
            e.printStackTrace();
        }
        closeConnection();
    }

    //Обновляем изображения товара. Если имя товара поменялось при редактировании, удаляем старую директорию и создаем новую.
    public void updateImages(ShopItem oldVersionItem, ShopItem newVersionItem){
        setConnection();
        InputStream inputStream;
        String oldFileName;
        String newFileName;
        String[] oldFolders = oldVersionItem.getImageLink().split("/");
        String[] newFolders = newVersionItem.getImageLink().split("/");
        oldFileName = oldFolders[oldFolders.length - 1];
        newFileName = newFolders[newFolders.length - 1];
        if(!oldFileName.equals(newFileName)){
            try {
                fileClient.deleteFile(SERVER_FOLDER_FOR_IMAGES + oldVersionItem.getAlias() + "/" + Transliterator.transliteration(oldFileName));
                if(!newVersionItem.getImageLink().contains("http")){
                    inputStream = new FileInputStream(newVersionItem.getImageLink());
                } else {
                    URL sourceUrl = new URL(newVersionItem.getImageLink());
                    inputStream = sourceUrl.openStream();
                }
                fileClient.storeFile(SERVER_FOLDER_FOR_IMAGES + newVersionItem.getAlias() + "/" + Transliterator.transliteration(newFileName), inputStream);
                newVersionItem.setImageLink("http://mebelpokarmany.ru/assets/images/products/application/" + newVersionItem.getAlias() + "/" + Transliterator.transliteration(newFileName));
                inputStream.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        List<String> oldGalleryFileNames = new ArrayList<>();
        List<String> newGalleryFileNames = new ArrayList<>();
        for(String oldGalleryItem : oldVersionItem.getGallery()){
            oldFolders = oldGalleryItem.split("/");
            if(oldFolders[oldFolders.length - 1].endsWith("]")){
                oldGalleryFileNames.add(oldFolders[oldFolders.length - 1].replaceAll("]$", ""));
            } else {
                oldGalleryFileNames.add(oldFolders[oldFolders.length - 1]);
            }
        }
        for(String newGalleryItem : newVersionItem.getGallery()){
            newFolders = newGalleryItem.split("/");
            if(newFolders[newFolders.length - 1].endsWith("]")){
                newGalleryFileNames.add(newFolders[newFolders.length - 1].replaceAll("]$", ""));
            } else {
                newGalleryFileNames.add(newFolders[newFolders.length - 1]);
            }
        }

        for(String fileName : oldGalleryFileNames){
            if(!newGalleryFileNames.contains(fileName)){
                try{
                    fileClient.deleteFile(SERVER_FOLDER_FOR_IMAGES + oldVersionItem.getAlias() + "/" + Transliterator.transliteration(fileName));
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        List<String> newItemGallery = new ArrayList<>(newVersionItem.getGallery());
        newVersionItem.clearGallery();
        for(String fileName : newGalleryFileNames){
            if(!oldGalleryFileNames.contains(fileName)){
                for(String newGalleryItem : newItemGallery){
                    if(newGalleryItem.endsWith(fileName)){
                        try {
                            if (!newGalleryItem.contains("http")) {
                                inputStream = new FileInputStream(newGalleryItem);
                            } else {
                                URL sourceUrl = new URL(newGalleryItem);
                                inputStream = sourceUrl.openStream();
                            }
                            fileClient.storeFile(SERVER_FOLDER_FOR_IMAGES + newVersionItem.getAlias() + "/" + Transliterator.transliteration(fileName), inputStream);
                            newVersionItem.addGalleryItem("http://mebelpokarmany.ru/assets/images/products/application/" + newVersionItem.getAlias() + "/" + Transliterator.transliteration(fileName));
                            inputStream.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        closeConnection();
    }
}
