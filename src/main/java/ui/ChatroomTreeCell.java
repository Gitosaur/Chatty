package ui;

import controllers.Chatty;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import entities.Chatroom;
import entities.HabboInfo;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Optional;

public class ChatroomTreeCell extends TreeCell<Object> {

    private Chatty chatty;
    public ChatroomTreeCell(Chatty chatty) {
        this.chatty = chatty;
    }

    @Override
    protected void updateItem(Object o, boolean empty) {
        super.updateItem(o, empty);

        if(empty){
            setText(null);
            setGraphic(null);
            return;
        }

        if(o instanceof Chatroom) {
            Chatroom r = (Chatroom) o;
            this.getTreeItem().setExpanded(r.isExpanded());

            HBox roomBox = new HBox();
            roomBox.setSpacing(10);
            roomBox.setAlignment(Pos.CENTER_LEFT);
            Text expandIcon = GlyphsDude.createIcon(r.isExpanded() ? FontAwesomeIcon.CARET_DOWN : FontAwesomeIcon.CARET_RIGHT);
            expandIcon.getStyleClass().add("expandIcon");
            expandIcon.setFill(new Color(0.77, 0.77, 0.77, 1));
            expandIcon.setOnMouseClicked(e -> {
                this.getTreeItem().setExpanded(!r.isExpanded());
                r.setExpanded(!r.isExpanded());
                e.consume();
            });
            roomBox.getChildren().add(expandIcon);

            HBox userCountBox = new HBox();
            userCountBox.setAlignment(Pos.CENTER);
            Text userIcon = GlyphsDude.createIcon(FontAwesomeIcon.USER);
            userIcon.setFill(new Color(0.77, 0.77, 0.77, 1));
            userIcon.getStyleClass().add("icon");
            userCountBox.getChildren().add(userIcon);
            userCountBox.getChildren().add(new Label(String.valueOf(r.getUsers().size())));
            roomBox.getChildren().add(userCountBox);

            // icon for password
            if(r.hasPassword()) {
                Text passwordIcon = GlyphsDude.createIcon(FontAwesomeIcon.LOCK);
                passwordIcon.setFill(new Color(0.77, 0.77, 0.77, 1));
                roomBox.getChildren().add(passwordIcon);
            }

            //roomname
            Label roomNameLabel = new Label(Chatty.shortenString(r.getName(), 20));
            roomNameLabel.getStyleClass().add("roomName");
            roomBox.getChildren().add(roomNameLabel);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            roomBox.getChildren().add(spacer);

            if(r.getUsers().contains(chatty.getHabboInfo())){
                Button leaveBtn = new Button("leave");
                leaveBtn.getStyleClass().add("leaveBtn");
                roomBox.getChildren().add(leaveBtn);
                leaveBtn.setOnMouseClicked(e -> {
                    Optional<ButtonType> result = chatty.showConfirmDialog("Do you want to leave " + Chatty.shortenString(r.getName(), 7));
                    result.ifPresent(res -> {
                        chatty.unblurMainWindow();
                        if(res.getButtonData() == ButtonBar.ButtonData.YES){
                            chatty.leaveRoom(r.getName());
                        }
                    });
                });
            }
            roomBox.setOnMouseClicked(event -> chatty.joinChatroom(event, r.getName()));

            setGraphic(roomBox);
        }else if(o instanceof HabboInfo) {
            HabboInfo user = (HabboInfo) o;
            HBox userBox = new HBox();
            userBox.setAlignment(Pos.CENTER_LEFT);

            ImageView userImgView = user.getHabboHeadImage();

            Label userLabel = new Label(user.getHabboName());
            Label userHotel = new Label(user.getHotel().toString());
            userBox.getChildren().addAll(userImgView, userHotel, userLabel);
            setGraphic(userBox);
        }else {
            setText(null);
            setGraphic(null);
        }
    }



}
