package controllers;

import entities.HabboInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.text.SimpleDateFormat;

public class ChatlogController {

    private ListView<ChatInstance> listView;
    private ObservableList<ChatInstance> chatlist = FXCollections.observableArrayList();

    public ChatlogController(ListView<ChatInstance> listView) {
        this.listView = listView;
        this.listView.setItems(chatlist);
        this.listView.setCellFactory(r -> new ChatCell());
    }

    public void addChat(HabboInfo user, String msg, String room) {
        Platform.runLater(() -> {
            this.chatlist.add(new ChatInstance(user, msg, room));
            this.listView.scrollTo(listView.getItems().size() - 1);
        });
    }

    static class ChatCell extends ListCell<ChatInstance> {

        @Override
        public void updateItem(ChatInstance item, boolean empty) {
            super.updateItem(item, empty);

            if(item == null || empty) {
                setGraphic(null);
            }

            if (item != null) {
                HBox box = new HBox();
                box.setAlignment(Pos.CENTER_LEFT);
                box.setSpacing(5);

                Label msgLabel = new Label(item.msg);

                msgLabel.setWrapText(true);
                msgLabel.setTextAlignment(TextAlignment.JUSTIFY);
//                msgLabel.setMaxWidth(350);

                box.getChildren().add(new Label(item.timestamp));
                box.getChildren().add(new Label("["+item.room+"]"));
                box.getChildren().add(item.user.getHabboHeadImage());
                box.getChildren().add(new Label(item.user.getHabboName()+": "));
                box.getChildren().add(msgLabel);

                setGraphic(box);
            }
        }
    }

    private class ChatInstance {
        String room;
        HabboInfo user;
        String msg;
        String timestamp;

        public ChatInstance(HabboInfo user, String msg, String room) {
            this.user = user;
            this.msg = msg;
            this.room = room;
            this.timestamp = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        }
    }

}
