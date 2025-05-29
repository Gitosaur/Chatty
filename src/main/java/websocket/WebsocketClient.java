package websocket;

import controllers.Chatty;
import message.ChatMsg;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebsocketClient extends org.java_websocket.client.WebSocketClient{

    private Chatty chatty;
    private boolean connected;

    public WebsocketClient(URI serverUri, Chatty chatty) {
        super(serverUri);
        this.chatty = chatty;
        this.connected = false;
    }

    public void send(ChatMsg msg) {
//        System.out.println("SEND OUT: "+msg.toString());
        if(connected)
            super.send(msg.toString());
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.connected = true;
        this.chatty.onWebsocketOpen();
    }

    @Override
    public void onMessage(String s) {
        try {
            ChatMsg msg = ChatMsg.parse(s);
            this.chatty.onWebsocketMessage(msg);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("CONNECTION LOST");
        this.connected = false;
        this.chatty.onWebsocketDisconnect();
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        this.chatty.onWebsocketError();
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean b) {
        this.connected = b;
    }
}
