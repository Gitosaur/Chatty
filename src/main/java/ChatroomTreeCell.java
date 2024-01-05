import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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
            if(r.hasPassword())
                roomBox.getChildren().add(GlyphsDude.createIcon(FontAwesomeIcon.LOCK));

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
