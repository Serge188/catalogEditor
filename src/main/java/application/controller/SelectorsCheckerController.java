package application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class SelectorsCheckerController {
    Stage selectorStage;

    @FXML
    private Button runButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField urlField;

    @FXML
    private TextField selectorField;

    @FXML
    private TextArea elementArea;

    @FXML
    private void handleRun(){
        String url = urlField.getText();
        String selector = selectorField.getText();
        if(!url.isEmpty() && !selector.isEmpty()){
            try {
                Document doc = Jsoup.connect(url).get();
                Elements selectedEls = doc.select(selector);
                StringBuilder sb = new StringBuilder();
                for(Element el : selectedEls){
                    sb.append(el.html());
                }
                elementArea.setText(sb.toString());
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel(){
        selectorStage.close();
    }



    public void setStage(Stage stage){
        this.selectorStage = stage;
    }

}
