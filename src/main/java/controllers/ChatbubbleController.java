package controllers;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;

public class ChatbubbleController {

    private HabboClientController habboClientController;
    private ComboBox<Image> comboBox;
    private HashMap<Integer, Integer> choices;

    private static final int[] chatbubbleIds = {0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27, 30, 31, 32, 33, 35, 36, 37, 38, 120, 121, 130, 131, 132, 133, 1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1010, 1011, 1012, 1013, 1014};


    public ChatbubbleController(HabboClientController habboClientController, ComboBox<Image> comboBox) {
        this.habboClientController = habboClientController;
        this.comboBox = comboBox;
        this.choices = new HashMap<>();

        this.comboBox.setPrefWidth(100);

        Platform.runLater(() -> {
            comboBox.setCellFactory(listView -> new ImageCell());
            comboBox.setButtonCell(new ImageCell());
        });


        comboBox.setOnAction((event) -> {
           int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();
           int bubbleId = choices.get(selectedIndex);
           this.habboClientController.setChatbubble(bubbleId);
        });



        loadImages();
    }


    private void loadImages() {
        try {

            for (int idx = 0; idx < chatbubbleIds.length; idx++) {
                int cbId = chatbubbleIds[idx];
                Image img = new Image("/chatbubbles/" + cbId + ".png");
                choices.put(idx, cbId);
                comboBox.getItems().add(img);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


   private class ImageCell extends ListCell<Image> {
       @Override
       protected void updateItem(Image item, boolean empty) {
           super.updateItem(item, empty);

           if (item == null || empty) {
                setGraphic(null);
           } else {
               setGraphic(new ImageView(item));
           }
       }
   }


}
