package controllers;

import gearth.protocol.HMessage;
import gearth.protocol.HPacket;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AntiIdleController {

    private boolean enabled;
    private final int waitTime = 2 * 60 + 50;

    public AntiIdleController(Chatty chatty, boolean enabled) {
        this.enabled = enabled;

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (this.enabled && chatty.isGearthConnected()){
                chatty.sendToServer(new HPacket("AvatarExpression", HMessage.Direction.TOSERVER, 0));
            }
        }, 0, waitTime, TimeUnit.SECONDS);
    }

    public void setEnabled(boolean b) {
        this.enabled = b;
    }
}
