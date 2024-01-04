import entities.Chatroom;
import entities.HabboInfo;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

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

            Glyph expandIcon = new Glyph("FontAwesome", r.isExpanded() ? FontAwesome.Glyph.CARET_DOWN : FontAwesome.Glyph.CARET_RIGHT);
            expandIcon.getStyleClass().add("expandIcon");
            expandIcon.setOnMouseClicked(e -> {
                System.out.println("expanding");
                this.getTreeItem().setExpanded(!r.isExpanded());
                r.setExpanded(!r.isExpanded());
                e.consume();
            });
            roomBox.getChildren().add(expandIcon);

            HBox userCountBox = new HBox();
            userCountBox.setAlignment(Pos.CENTER);
            userCountBox.getChildren().add(new Glyph("FontAwesome", FontAwesome.Glyph.USER));
            userCountBox.getChildren().add(new Label(String.valueOf(r.getUsers().size())));
            roomBox.getChildren().add(userCountBox);

            // icon for password
            if(r.hasPassword())
                roomBox.getChildren().add(new Glyph("FontAwesome", FontAwesome.Glyph.LOCK));

            //roomname
            Label roomNameLabel = new Label(Chatty.shortenString(r.getName(), 20));
            roomNameLabel.getStyleClass().add("roomName");
            roomBox.getChildren().add(roomNameLabel);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            roomBox.getChildren().add(spacer);

            if(r.getUsers().contains(chatty.getHabboInfo())){
                Button leaveBtn = new Button("LEAVE");
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
            roomBox.setOnMouseClicked(event -> chatty.roomClicked(event, r.getName()));

            setGraphic(roomBox);
        }else if(o instanceof HabboInfo) {
            HabboInfo user = (HabboInfo) o;
            HBox userBox = new HBox();
            userBox.setAlignment(Pos.CENTER_LEFT);
            ImageView userImgView = new ImageView();
            userImgView.setPreserveRatio(true);
            userImgView.setFitWidth(35);

            if(user.getHeadImg() == null) {
                userImgView.setImage(new Image("/avatar-head-placeholder.png"));
                if(!user.imageLoading()){
                    Image headImg = new Image(getFigureStringUrl(user.getFigureStr()), true);
                    headImg.progressProperty().addListener((observable, oldValue, progress) -> {
                        user.setImageLoading(true);
                        if ((Double) progress == 1.0 && !headImg.isError()) {
                            user.setHeadImg(headImg);
                            user.setImageLoading(false);
                            userImgView.setImage(headImg);
                        }
                    });
                }
            }else {
                userImgView.setImage(user.getHeadImg());
            }

            Label userLabel = new Label(user.getHabboName());
            Label userHotel = new Label(user.getHotel().toString());
            userBox.getChildren().addAll(userImgView, userHotel, userLabel);
            setGraphic(userBox);
        }else {
            setText(null);
            setGraphic(null);
        }
    }

    private String getFigureStringUrl(String figure) {
        return "https://www.habbo.com/habbo-imaging/avatarimage?size=b&figure="+figure+"&headonly=1";
    }

}
