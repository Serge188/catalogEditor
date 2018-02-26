package application.services;

import java.util.HashMap;
import java.util.Map;


public class Transliterator {

    //Метод для транслитерации названия товара (нужен для формирования ссылки)
    public static String transliteration(String string){
        String lowerString = string.toLowerCase();
        StringBuilder sb = new StringBuilder();
        Map<Character, String> map = new HashMap<>();
        map.put('а', "a");
        map.put('б', "b");
        map.put('в', "v");
        map.put('г', "g");
        map.put('д', "d");
        map.put('е', "e");
        map.put('ё', "e");
        map.put('ж', "zh");
        map.put('з', "z");
        map.put('и', "i");
        map.put('к', "k");
        map.put('л', "l");
        map.put('м', "m");
        map.put('н', "n");
        map.put('о', "o");
        map.put('п', "p");
        map.put('р', "r");
        map.put('с', "s");
        map.put('т', "t");
        map.put('у', "u");
        map.put('ф', "f");
        map.put('х', "kh");
        map.put('ц', "ts");
        map.put('ч', "ch");
        map.put('ш', "sh");
        map.put('щ', "sh");
        map.put('ъ', "");
        map.put('ы', "i");
        map.put('ь', "");
        map.put('э', "e");
        map.put('ю', "u");
        map.put('я', "ya");

        for(int i = 0; i < lowerString.length(); i++){
            if (map.containsKey(lowerString.charAt(i))){
                sb.append(map.get(lowerString.charAt(i)));
            } else {
                sb.append(lowerString.charAt(i));
            }
        }
        return sb.toString().replaceAll(" ", "_").replaceAll("[^0-9a-z\\.]", "");
    }
}
