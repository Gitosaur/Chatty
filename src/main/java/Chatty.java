import crypto.AES;
import crypto.CryptoUtils;
import crypto.DiffieHellman;
import entities.Chatroom;
import entities.HabboId;
import entities.HabboInfo;
import entities.Hotel;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gearth.protocol.connection.HClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import message.ChatMsg;
import message.ChatMsgData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.ResourceBundle;

@ExtensionInfo(
        Title =  "Chatty",
        Description =  "Private messenger - cross-room, cross-hotel",
        Version =  "2.0",
        Author =  "Gitosaur"
)
public class Chatty extends ExtensionForm implements Initializable {

//    private static final String DEFAULT_WS_SERVER_URL = "ws://localhost:8000";
    private static final String DEFAULT_WS_SERVER_URL = "ws://49.13.194.116:8000";

    private WebsocketClient ws;

    private HabboInfo habboInfo;
    private Hotel hotel;

    private LinkedHashMap<String, Chatroom> chatrooms;

    // when joining a room, a new DH key exchange is taking place - the key is stored here
    private HashMap<String, DiffieHellman> chatroomRequests;

    private HabboChatController habboChatController;
    private ChatlogController chatlogController;


    private boolean active; //secret chat active
    private boolean showHotelsInClient;
    private boolean receiveInformationInClient;
    private boolean showTypingSpeechBubble;

    private Stage stage;

    @FXML public TreeView chatroomsView;
    @FXML public Button connectToggleButton;
    @FXML public Button settingsConnectButton;
    @FXML public Button createRoomButton;

    @FXML public RadioButton activeToggle;
    @FXML public RadioButton alwaysOnTopToggle;
    @FXML public RadioButton showTypingSpeechBubbleToggle;

    @FXML public Circle serverStatusCircle;
    @FXML public Label serverConnectStatusLabel;

    @FXML public TextField websocketServerUrlTextField;
    @FXML public TabPane tabPane;
    @FXML public Region opaqueLayer;

    @FXML public VBox contentPane;

    @FXML public RadioButton receiveInfoInClientRadioButton;
    @FXML public RadioButton showHotelsInClientRadioButton;

    @FXML public ComboBox<Image> chatbubbleComboBox;
    @FXML public ListView chatlogListView;


    private boolean gEarthConnected;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.chatrooms = new LinkedHashMap<String, Chatroom>();
        this.chatroomRequests = new HashMap<>();

        this.gEarthConnected = false;
        this.active = true;
        this.showHotelsInClient = true;
        this.receiveInformationInClient = true;
        this.showTypingSpeechBubble = true;

        this.createRoomButton.setVisible(false);
        this.opaqueLayer.setVisible(false);

        this.chatroomsView.setShowRoot(false);
        this.chatroomsView.setRoot(new TreeItem());
        this.chatroomsView.setCellFactory(treeView -> new ChatroomTreeCell(this));

        websocketServerUrlTextField.setText(DEFAULT_WS_SERVER_URL);

        initializeRadioButtons();
    }

    private void initializeRadioButtons() {
        this.activeToggle.setSelected(this.active);
        this.activeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> this.active = newValue);

        this.alwaysOnTopToggle.setSelected(false);
        this.alwaysOnTopToggle.selectedProperty().addListener((observable, oldValue, newValue) -> this.stage.setAlwaysOnTop(newValue));

        this.receiveInfoInClientRadioButton.setSelected(this.receiveInformationInClient);
        this.receiveInfoInClientRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> this.receiveInformationInClient = newValue);

        this.showHotelsInClientRadioButton.setSelected(this.showHotelsInClient);
        this.showHotelsInClientRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.showHotelsInClient = newValue;
            if(habboChatController != null)
                habboChatController.respawnUserDummys();
        });

        this.showTypingSpeechBubbleToggle.setSelected(this.showTypingSpeechBubble);
        this.showTypingSpeechBubbleToggle.selectedProperty().addListener((observable, oldValue, newValue) -> this.showTypingSpeechBubble = newValue);

        this.websocketServerUrlTextField.textProperty().addListener((observable, oldValue, newValue) -> websocketServerUrlOnChange(newValue));

        this.updateServerStatusUi();
    }

    @Override
    protected void initExtension() {
        onConnect(this::onGearthConnect);

        this.habboChatController = new HabboChatController(this);
        new ChatbubbleController(habboChatController, this.chatbubbleComboBox);
        this.chatlogController = new ChatlogController(this, this.chatlogListView);

        intercept(HMessage.Direction.TOCLIENT, "UserObject", hMessage -> {
            HPacket packet = hMessage.getPacket();
            int id = packet.readInteger();
            String name = packet.readString();
            String figure = packet.readString();
            String sex = packet.readString();
            String mission = packet.readString();
            this.habboInfo = new HabboInfo(id, name, figure, sex, mission, hotel);

            //grab the url
            String wsUrl = this.websocketServerUrlTextField.getText();
            this.connectToWsServer(wsUrl);
        });
    }

    @Override
    protected void onEndConnection() {
        super.onEndConnection();
        System.out.println("GEARTH ON END CONNECTION");
        this.gEarthConnected = false;
        this.chatrooms.clear();
        this.chatroomRequests.clear();
        this.habboChatController.clearAllDummys();
        if(ws != null) this.ws.close();
        this.updateUi();
    }

    private void onGearthConnect(String host, int port, String hotelversion, String clientIdentifier, HClient clientType) {
        gEarthConnected = true;
        switch (host) {
            case "game-us.habbo.com": this.hotel = Hotel.US; break;
            case "game-nl.habbo.com": this.hotel = Hotel.NL; break;
            case "game-br.habbo.com": this.hotel = Hotel.BR; break;
            case "game-tr.habbo.com": this.hotel = Hotel.TR; break;
            case "game-de.habbo.com": this.hotel = Hotel.DE; break;
            case "game-fr.habbo.com": this.hotel = Hotel.FR; break;
            case "game-fi.habbo.com": this.hotel = Hotel.FI; break;
            case "game-es.habbo.com": this.hotel = Hotel.ES; break;
            case "game-it.habbo.com": this.hotel = Hotel.IT; break;
            case "game-s2.habbo.com": this.hotel = Hotel.S2; break;
        }
    }

    @Override
    protected void onStartConnection() {
        updateUi();
    }

    private void connectToWsServer(String url) {
        try {
            setStatusConnectingUi();
            if(ws != null && ws.isConnected())
                ws.close();

            ws = new WebsocketClient(new URI(url), this);
            ws.connect();
        } catch (URISyntaxException e) {
            showErrorDialog("Invalid URL syntax");
            unblurMainWindow();
        }
        updateUi();
    }

    protected void onWebsocketOpen() {
        ws.send(new ChatMsg("connect", habboInfo.serialize()));
    }

    private void updateUi() {
        Platform.runLater(() -> {
            this.createRoomButton.setVisible(this.ws != null && this.ws.isConnected());
            updateChatroomsUi();
            updateServerStatusUi();
            updateSettingsUi();
        });
    }


    protected void onWebsocketMessage(ChatMsg msg) {
        System.out.println("INCOMING: " + msg);
        String type = msg.getType();

        switch(type) {
            case "connect": onWebsocketConnect(msg.getData()); break;
            case "user_joined": onUserJoined(msg.getData()); break;
            case "user_left": onUserLeft(msg.getData()); break;
            case "message": onMessage(msg.getData()); break;
            case "show_rooms": onShowRooms(msg.getData()); break;
            case "new_room": onNewRoom(msg.getData()); break;
            case "user_move": onUserMove(msg.getData()); break;
            //case "room_info": onRoomInfo(msg.getData()); break; // not needed for gui version
            //case "room_users": onRoomUsers(msg.getData()); break; // not needed for gui version

            case "room_key_request": onKeyRequest(msg.getData()); break;
            case "room_key": onKeyIncoming(msg.getData()); break;
        }

        if(type.contains("_error")) {
            Platform.runLater(() -> {
                showErrorDialog((String) msg.getData().get("message"));
                unblurMainWindow();
            });
        }
    }

    /**
     * Is called when the server sends you the shared AES key for a chatroom
     * with the public dh-key of the host. First it creates the shared DH-key to decrypt
     * the chatroom AES key
     */
    private void onKeyIncoming(ChatMsgData data) {
        try {
            String room = (String) data.get("room");
            String dhPub = (String) data.get("dh_pub");
            String encRoomKey = (String) data.get("room_key");
            String iv = (String) data.get("iv");

            DiffieHellman dh = chatroomRequests.get(room);
            byte[] sharedDhSecret = dh.generateSharedSecret(dhPub);

            AES aes = new AES(sharedDhSecret);
            String decryptedRoomKey = aes.decrypt(encRoomKey, iv);

            AES roomAes = new AES(CryptoUtils.b64decode(decryptedRoomKey));
            this.chatrooms.get(room).setEncryption(roomAes);

            this.chatroomRequests.remove(room);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Is called when the server requests you (the host) to encrypt the shared room AES key
     * With the DiffieHellman public key inside the request MsgData
     */
    private void onKeyRequest(ChatMsgData data) {
        try {
            String roomname = (String) data.get("room");
            String otherDhPub = (String) data.get("dh_pub");
            String clientId = (String) data.get("clientId");

            //check if you have the roomkey
            Chatroom room = chatrooms.get(roomname);
            if(room == null || room.getEncryption() == null) {
                // tell server that you dont have the shared key for that room
                System.out.println("No AES Key for this room");
                return;
            }

            DiffieHellman dh = new DiffieHellman();
            KeyPair keyPair = dh.getKeyPair();
            if(keyPair != null){
                String roomKey = CryptoUtils.b64encode(room.getEncryption().getSecretKey().getEncoded());

                byte[] sharedDhSecret = dh.generateSharedSecret(otherDhPub);
                AES aes = new AES(sharedDhSecret);

                String[] encryption = aes.encrypt(roomKey);
                String encryptedRoomKey = encryption[0];
                String iv = encryption[1];

                System.out.println("encryptedRoomKey: " + encryptedRoomKey);

                ChatMsg msg = new ChatMsg("room_key");
                ChatMsgData msgData = new ChatMsgData();
                msg.setData(msgData);

                msgData.put("room", roomname);
                msgData.put("for_client", clientId);
                msgData.put("dh_pub", CryptoUtils.b64encode(dh.getKeyPair().getPublic().getEncoded()));
                msgData.put("room_key", encryptedRoomKey);
                msgData.put("iv", iv);
                ws.send(msg);
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void onShowRooms(ChatMsgData data) {
        JSONArray rooms = (JSONArray) data.get("rooms");
        this.chatrooms.clear();
        for (Object o : rooms) {
            JSONObject room = (JSONObject) o;
            String roomName = room.getString("name");
            boolean password = room.getBoolean("password");

            HashMap<HabboId, HabboInfo> userList = new HashMap<>();
            JSONArray usersJson = room.getJSONArray("users");
            for (int i = 0; i < usersJson.length(); i++) {
                try {
                    JSONObject user = usersJson.getJSONObject(i);
                    String username = user.getString("name");
                    String mission = user.getString("mission");
                    String figureStr = user.getString("figure");
                    String sex = user.getString("sex");
                    Hotel hotel = Hotel.valueOf(user.getString("hotel"));

                    userList.put(new HabboId(username, hotel), new HabboInfo(username, figureStr, sex, mission, hotel));
                    this.habboChatController.addDummy(username, hotel, roomName, mission, figureStr, sex);
                }catch(JSONException e) {
                    e.printStackTrace();
                    System.err.println("Could not extract data from JSON");
                }
            }

            Chatroom searched = chatrooms.get(roomName);
            if(searched != null) {
                searched.setHasPassword(password);
                searched.setUsers(userList);
            }else {
                this.chatrooms.put(roomName, new Chatroom(roomName, userList, password));
            }
        }
        updateUi();
    }

    private void onMessage(ChatMsgData data) {
        String habbo = (String) data.get("habbo");
        Hotel hotel = Hotel.valueOf((String) data.get("hotel"));
        String room = (String) data.get("room");
        String msg = (String) data.get("message");
        String type = (String) data.get("type");
        String iv = (String) data.get("iv");
        int style = (int) data.get("style");
        boolean shout = type.equals("Shout");

        Chatroom chatroom = this.chatrooms.get(room);
        if(chatroom == null || chatroom.getEncryption() == null) {
            this.habboChatController.sendInformationMsg("You don't have the room encryption key yet");
            System.err.println("No encryption for "+room);
            return;
        }

        String decrypted = this.chatrooms.get(room).getEncryption().decrypt(msg, iv);

        this.chatlogController.addChat(findUser(habbo, hotel), decrypted, room);
        this.habboChatController.sendChat(habbo, hotel, room, decrypted, style, shout);
    }

    private void onUserLeft(ChatMsgData data) {
        String room = (String) data.get("room");
        String user = (String) data.get("name");
        Hotel hotel = Hotel.valueOf((String) data.get("hotel"));
        Chatroom chatroom = chatrooms.get(room);

        boolean isSelf = isSelf(user, hotel);

        if(isMemberOfRoom(room))
            habboChatController.sendInformationMsg((isSelf ? "You" : user) + " left "+ room);

        if(chatroom != null) {
            chatroom.removeUser(user, hotel);
            if(chatroom.getUsers().size() == 0) {
                this.chatrooms.remove(chatroom.getName());
            }
        }
        this.habboChatController.removeDummy(user, room);
        updateUi();
    }

    private void onUserJoined(ChatMsgData data) {
        String username = (String) data.get("name");
        String mission = (String) data.get("mission");
        String figure = (String) data.get("figure");
        String sex = (String) data.get("sex");
        String room = (String) data.get("room");
        Hotel hotel = Hotel.valueOf((String) data.get("hotel"));

        HabboInfo userInfo = findUser(username, hotel);
        if(userInfo == null)
            userInfo = new HabboInfo(username, figure, sex, mission, hotel);

        Chatroom chatroom = chatrooms.get(room);
        if(chatroom != null){
            chatroom.addUser(userInfo);
        }else {
            //TODO create room
        }
        this.habboChatController.addDummy(username, hotel, room, mission, figure, sex);
        boolean isSelf = isSelf(username, hotel);

        if(isMemberOfRoom(room)){
            chatroom.setExpanded(true);
            habboChatController.sendInformationMsg((isSelf ? "You" : username) + " joined "+ room);
        }
        updateUi();
    }

    private void onNewRoom(ChatMsgData data) {
        String roomName = (String) data.get("name");
        boolean hasPwd = (boolean) data.get("password");
        JSONObject creator = (JSONObject) data.get("creator");

        String username = creator.getString("name");
        String mission = creator.getString("mission");
        String figureStr = creator.getString("figure");
        String sex = creator.getString("sex");
        Hotel hotel = Hotel.valueOf(creator.getString("hotel"));

        HabboInfo creatorInfo = findUser(username, hotel);
        if(creatorInfo == null)
            creatorInfo = new HabboInfo(username, figureStr, sex, mission, hotel);


        Chatroom room = null;
        if(isSelf(username, hotel)) {
            try {
                room = new Chatroom(roomName, hasPwd, new AES());
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorDialog("ERROR could not initialize encryption"));
            }
        }else {
            room = new Chatroom(roomName, hasPwd);
        }

        room.addUser(creatorInfo);
        this.chatrooms.put(roomName, room);
        this.habboChatController.addDummy(username, hotel, roomName, mission, figureStr, sex);

        if(isMemberOfRoom(roomName)) {
            room.setExpanded(true);
            habboChatController.sendInformationMsg((username.equals(this.habboInfo.getHabboName()) ? "You" : username) + " joined "+ roomName);
        }

        updateUi();
    }

    private boolean isMemberOfRoom(String room) {
        Chatroom r = this.chatrooms.get(room);
        if(r == null)
            return false;

        for(HabboInfo user: r.getUsers()) {
            if(user.getHabboName().equals(this.habboInfo.getHabboName()) &&
            user.getHotel() == this.habboInfo.getHotel()){
                return true;
            }
        }
        return false;
    }

    private void updateSettingsUi() {
        if(ws == null) return;
        String url = ws.getURI().toString();
        this.websocketServerUrlTextField.setText(url);
    }

    private void updateServerStatusUi() {
        Color connected = new Color(0.2053, 0.8289, 0.0806, 1.0);
        Color disconnected = new Color(0.82, 0.082, 0.082, 1.0);
        this.connectToggleButton.setDisable(false);

        if(ws == null || !ws.isConnected()){
            settingsConnectButton.setDisable(false);
            this.serverConnectStatusLabel.setText("Server disconnected");
            this.serverStatusCircle.setFill(disconnected);
            this.serverStatusCircle.setEffect(new DropShadow(5, disconnected));
            this.connectToggleButton.setText("connect");
        }else if(ws.isConnected()){
            settingsConnectButton.setDisable(true);
            this.serverConnectStatusLabel.setText("Server connected");
            this.serverStatusCircle.setFill(connected);
            this.serverStatusCircle.setEffect(new DropShadow(5, connected));
            this.connectToggleButton.setText("disconnect");
        }

    }

    private void updateChatroomsUi() {
        TreeItem rootItem = new TreeItem();

        for (Chatroom r : chatrooms.values()) {
            TreeItem roomTreeItem = new TreeItem(r);
            rootItem.getChildren().add(roomTreeItem);

            for (HabboInfo user : r.getUsers()) {
                TreeItem userTreeItem = new TreeItem(user);
                roomTreeItem.getChildren().add(userTreeItem);
            }
        }

        this.chatroomsView.setRoot(rootItem);
    }

    protected void leaveRoom(String name) {
        ChatMsg msg = new ChatMsg("leave_room");
        ChatMsgData data = new ChatMsgData();
        data.put("room", name);
        msg.setData(data);
        ws.send(msg);
    }

    private HabboInfo findUser(String username, Hotel hotel) {
        for(Chatroom r: chatrooms.values()){
            for(HabboInfo h: r.getUsers()){
                if(h.getHabboName().equals(username) && h.getHotel() == hotel)
                    return h;
            }
        }
        return null;
    }


    /**
     * Send a join room request
     */
    protected void joinChatroom(MouseEvent event, String roomName) {
        if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
            Chatroom room = chatrooms.get(roomName);

            if(room == null) return;

            //check if habbo is already member of room
            if(room.getUsers().contains(this.habboInfo)) {
                showErrorDialog("You are already member of " + shortenString(roomName, 7));
                unblurMainWindow();
                return;
            }

            HashMap<String, Object> values = new HashMap();
            values.put("room", roomName);
            values.put("name", habboInfo.getHabboName());
            values.put("figure", habboInfo.getFigureStr());
            values.put("sex", habboInfo.getSex());

            if(room.hasPassword()){
                Optional<String> result = showPasswordDialog("Enter password for " + shortenString(roomName, 7));
                result.ifPresent(pwd -> {
                    if(pwd.isEmpty())
                        return;
                    values.put("password", CryptoUtils.b64encode(CryptoUtils.sha256(pwd)));
                });
            }
            if(!room.hasPassword() || (room.hasPassword() && values.containsKey("password"))) {
                DiffieHellman dh = new DiffieHellman();
                this.chatroomRequests.put(roomName, dh);
                ChatMsg msg = new ChatMsg("join_room");
                ChatMsgData msgData = new ChatMsgData(values);
                msg.setData(msgData);
                msgData.put("dhPub", CryptoUtils.b64encode(dh.getKeyPair().getPublic().getEncoded()));
                ws.send(msg);
            }
        }
    }

    private void onWebsocketConnect(ChatMsgData data) {
        String status = (String) data.get("status");
        if(status != null && status.equals("success")){
            System.out.println("Connected to Websocket server!!");
            this.habboChatController.sendInformationMsg("Connected to server");
            ws.setConnected(true);
            ws.send(new ChatMsg("show_rooms"));
        }else {
            System.out.println("Connection to Websocket server failed");
            this.habboChatController.sendInformationMsg("Connection to server failed");
            showErrorDialog((String) data.get("message"));
            ws.setConnected(false);
        }
        updateUi();
    }

    public void onWebsocketDisconnect() {
        this.chatrooms.clear();
        if(gEarthConnected){
            this.habboChatController.sendInformationMsg("Disconnected from server");
            this.habboChatController.clearAllDummys();
        }
        this.updateUi();
    }

    //settings connect button triggers this
    public void setUrlAndConnect(ActionEvent actionEvent) {
        if(!gEarthConnected){
            showErrorDialog("G-Earth is not connected");
            unblurMainWindow();
            return;
        }

        if(ws == null) {
            setStatusConnectingUi();
            sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER));
            return;
        }

        String wsUrl = this.websocketServerUrlTextField.getText();
        if((ws.getURI().toString().equals(wsUrl) && !ws.isConnected()) ||
             !ws.getURI().toString().equals(wsUrl)){
            setStatusConnectingUi();
            sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER));
        }
    }

    private void setStatusConnectingUi() {
        Platform.runLater(() -> {
            this.connectToggleButton.setDisable(true);
            this.serverConnectStatusLabel.setText("connecting...");
            Color orange = new Color(1.0, 0.55, 0.0, 1.0);
            this.serverStatusCircle.setFill(orange);
            this.serverStatusCircle.setEffect(new DropShadow(5, orange));
        });
    }

    //connect button triggers this
    public void toggleConnect(ActionEvent actionEvent) {
        if(!gEarthConnected) {
            showErrorDialog("G-Earth is not connected");
            unblurMainWindow();
            return;
        }

        if(ws == null || !ws.isConnected()){
            setStatusConnectingUi();
            // this triggers a websocket connect on User
            sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER));
        }else {
            Optional<ButtonType> result = showConfirmDialog("Do you want to disconnect from the server?");
            unblurMainWindow();
            result.ifPresent(res -> {
                if(res.getButtonData().isDefaultButton())
                    this.ws.close();
            });
        }
        this.updateUi();
    }

    public void setDefaultServerURL(ActionEvent actionEvent) {
        this.websocketServerUrlTextField.setText(DEFAULT_WS_SERVER_URL);
        if(ws == null) return;
        this.settingsConnectButton.setDisable(isNewAndOrNotConnected(DEFAULT_WS_SERVER_URL));
    }

    public void onWebsocketError() {
        Platform.runLater(() -> {
            this.showErrorDialog("Could not connect to Server");
            unblurMainWindow();
        });
    }

    public void websocketServerUrlOnChange(String text) {
        if(ws == null) return;
        this.settingsConnectButton.setDisable(text.isEmpty() || isNewAndOrNotConnected(text));
    }

    private boolean isNewAndOrNotConnected(String newUrl){
        return (!ws.getURI().toString().equals(newUrl) || ws.isConnected()) && ws.getURI().toString().equals(newUrl);
    }

    private Optional<String> showPasswordDialog(String headerText) {
        DialogPane dialogPane = null;
        try {
            dialogPane = FXMLLoader.load(getClass().getResource("/dialogs/room-password-dialog.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.initOwner(this.stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialogPane.getScene().setFill(null);

        blurMainWindow();
        Label headerLabel = (Label) dialogPane.lookup("#headerLabel");
        headerLabel.setText(headerText);

        TextField password = (TextField) dialogPane.lookup("#chatroomPasswordInput");
        dialog.setResultConverter(dialogButton -> {
            unblurMainWindow();

            if (dialogButton == ButtonType.OK) {
                return password.getText();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public void createRoomButtonPressed(ActionEvent actionEvent) {

        DialogPane dialogPane = null;
        try {
            dialogPane = FXMLLoader.load(getClass().getResource("/dialogs/create-room-dialog.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.initOwner(this.stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle("Create chatroom");
        dialogPane.getScene().setFill(null);

        blurMainWindow();

        TextField roomname = (TextField) dialogPane.lookup("#chatroomNameInput");
        TextField password = (TextField) dialogPane.lookup("#chatroomPasswordInput");

        // Enable/Disable button depending on whether a name was entered.
        Node createBtn = dialog.getDialogPane().lookupButton(ButtonType.OK);
        createBtn.setDisable(true);

        roomname.textProperty().addListener((observable, oldValue, newValue) -> {
            createBtn.setDisable(newValue.trim().isEmpty());
        });

        Platform.runLater(roomname::requestFocus);

        // Convert the result to a roomname-password-pair
        dialog.setResultConverter(dialogButton -> {
            unblurMainWindow();

            if (dialogButton == ButtonType.OK) {
                return new Pair<>(roomname.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(namePw -> {
            String name = namePw.getKey();
            if(name.length() > 15)
                name = shortenString(name, 15);

            String pw = namePw.getValue();
            if(pw.length() > 15)
                pw = shortenString(name, 15);

            sendCreateRoomRequest(name, pw);
        });
    }

    private void sendCreateRoomRequest(String name, String pw) {
        ChatMsg msg = new ChatMsg("create_room");
        ChatMsgData data = new ChatMsgData();
        data.put("room", name);
        if(pw != null && !pw.isEmpty())
            data.put("password", CryptoUtils.b64encode(CryptoUtils.sha256(pw)));

        msg.setData(data);
        ws.send(msg);
    }

    public void broadcastMessage(String text, int style, boolean shout) {

        if(!active) return;
        //check if user is in any rooms
        Chatroom room = null;
        for(Chatroom r: this.chatrooms.values()){
            if(r.getUsers().contains(this.habboInfo)) {
                room = r;
            }
        }

        if(room == null) {
            habboChatController.sendInformationMsg("You are in no rooms");
            return;
        }

        ChatMsg msg = new ChatMsg("message");
        ChatMsgData data = new ChatMsgData();

        if(room.getEncryption() == null){
            this.habboChatController.sendInformationMsg("You don't have the room encryption key yet");
            return;
        }

        String[] encryption = room.getEncryption().encrypt(text);
        String encryptedMsg = encryption[0];
        String iv = encryption[1];

        data.put("message", encryptedMsg);
        data.put("iv", iv);
        data.put("style", style);
        data.put("type", shout ? "Shout" : "Chat");
        msg.setData(data);
        this.ws.send(msg);
    }

    public void sendUserMoved(int x, int y) {
        ChatMsg msg = new ChatMsg("user_move");
        ChatMsgData data = new ChatMsgData();
        data.put("x", x);
        data.put("y", y);
        msg.setData(data);
        if(ws != null)
            ws.send(msg);
    }

    public void onUserMove(ChatMsgData data) {
        String username = (String) data.get("name");
        Hotel hotel = Hotel.valueOf((String) data.get("hotel"));
        String room = (String) data.get("room");
        int x = (int) data.get("x");
        int y = (int) data.get("y");
        habboChatController.moveDummy(username, hotel, room, x, y);
    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void minimizeWindow(ActionEvent e) {
        stage.setIconified(true);
    }

    public void closeWindow(ActionEvent e) {
        stage.hide();
    }

    protected void unblurMainWindow() {
        this.opaqueLayer.setVisible(false);
        contentPane.setEffect(null);
    }

    private void blurMainWindow() {
        //background blur main pane
		GaussianBlur blur = new GaussianBlur(5);
		contentPane.setEffect(blur);
        this.opaqueLayer.setVisible(true);
    }

    protected Optional<ButtonType> showConfirmDialog(String headerText) {
        DialogPane dialogPane = null;
        try {
            dialogPane = FXMLLoader.load(getClass().getResource("/dialogs/confirm-dialog.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.initOwner(this.stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialogPane.getScene().setFill(null);

        Label headerLabel = (Label) dialogPane.lookup("#headerLabel");
        headerLabel.setText(headerText);

        blurMainWindow();

        return dialog.showAndWait();
    }

    private Optional<ButtonType> showErrorDialog(String headerText) {

        DialogPane dialogPane = null;
        try {
            dialogPane = FXMLLoader.load(getClass().getResource("/dialogs/error-dialog.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.initOwner(this.stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialogPane.getScene().setFill(null);

        Label headerLabel = (Label) dialogPane.lookup("#headerLabel");
        headerLabel.setText(headerText);

        blurMainWindow();

        return dialog.showAndWait();
    }

    public boolean sendInformationMsgEnabled() {
        return this.receiveInformationInClient;
    }

    public boolean displayHotelEnabled() {
        return this.showHotelsInClient;
    }

    protected static String shortenString(String n, int maxLen) {
        String s = n;
        if(n.length() > maxLen) {
            s = n.substring(0, maxLen) + "...";
        }
        return s;
    }

    public HabboInfo getHabboInfo() {
        return this.habboInfo;
    }

    public boolean showTypingSpeechBubble() {
        return this.showTypingSpeechBubble;
    }

    public boolean isActive() {
        return this.active;
    }

    private boolean isSelf(String name, Hotel hotel) {
        return name.equals(this.habboInfo.getHabboName()) && hotel == this.habboInfo.getHotel();
    }

    public boolean isGearthConnected() {
        return this.gEarthConnected;
    }
}



