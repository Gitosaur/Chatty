import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ChatbubbleController {

    private HabboChatController habboChatController;

    private ComboBox<Image> comboBox;

    private HashMap<Integer, Integer> choices;

    public ChatbubbleController(HabboChatController habboChatController, ComboBox<Image> comboBox) {
        this.habboChatController = habboChatController;
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
           this.habboChatController.setChatbubble(bubbleId);
        });



        loadImages();
    }


    private void loadImages() {
        try {
            URL path = getClass().getResource("/chatbubbles");
            File f = new File(path.toURI());
            List<File> files = Files.list(f.toPath())
                .map(Path::toFile)
                .sorted(Comparator.comparing(ChatbubbleController::extractNumberFromFileName))
                .filter(File::isFile)
                .collect(Collectors.toList());

            for (int i = 0; i < files.size(); i++) {
                File imgFile = files.get(i);
                choices.put(i, extractNumberFromFileName(imgFile));

                Image image = new Image(imgFile.getAbsolutePath());
                comboBox.getItems().add(image);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private static int extractNumberFromFileName(File file) {
        String numberString = file.getName().split("\\.")[0];
        return Integer.parseInt(numberString);
   }


   private static class ImageCell extends ListCell<Image> {
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
