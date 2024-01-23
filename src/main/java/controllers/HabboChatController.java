package controllers;

import entities.HabboInfo;
import entities.Hotel;
import gearth.extensions.parsers.HEntity;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HabboChatController {

    private static final double DUMMYS_HEIGHT_OFFSET = 600;
    private Chatty chatty;

    private Dummy infoDummy;
    private String infoDummyUserName = "[controllers.Chatty]";
    private String infoDummyMission = "";
    private String infoDummyFigureStr = "hd-4287-29.ch-4025-1428-95.lg-4017-1428-95.sh-4016-1243-110";
    private String infoDummySex = "M";
    private int infoDummyChatBubble = 31;

    private List<Dummy> dummys;
    private int chatbubbleId;

    public HabboChatController(Chatty chatty) {
        this.chatty = chatty;
        this.dummys = new ArrayList<Dummy>();
        this.chatbubbleId = -1;

        this.infoDummy = new Dummy(infoDummyUserName, null,  null, infoDummyMission, infoDummyFigureStr, infoDummySex);

        chatty.intercept(HMessage.Direction.TOSERVER, "Chat", hMessage -> {
            HPacket packet = hMessage.getPacket();
            String text = fixEncoding(packet.readString());
            int style = packet.readInteger();
            if(this.chatbubbleId != -1)
                style = this.chatbubbleId;

            hMessage.setBlocked(chatty.isActive());
            chatty.broadcastMessage(text, style, false);
        });

        chatty.intercept(HMessage.Direction.TOSERVER, "Shout", hMessage -> {
            HPacket packet = hMessage.getPacket();
            String text = fixEncoding(packet.readString());
            int style = packet.readInteger();
            if(this.chatbubbleId != -1)
                style = this.chatbubbleId;
            hMessage.setBlocked(chatty.isActive());
            chatty.broadcastMessage(text, style, true);
        });

        chatty.intercept(HMessage.Direction.TOCLIENT, "Users", hMessage -> {
            findAndSetUserIndex(hMessage);
            spawnDummyInClient(this.infoDummy);
            respawnUserDummys();
        });

        chatty.intercept(HMessage.Direction.TOCLIENT, "UserUpdate", hMessage -> {
            updateHabboPosition(hMessage);
        });

        chatty.intercept(HMessage.Direction.TOSERVER, "StartTyping", hMessage -> {
            hMessage.setBlocked(!chatty.showTypingSpeechBubble());
        });

    }

    private void findAndSetUserIndex(HMessage hMessage) {
        if(chatty.getHabboInfo() == null)
            return;

        HEntity[] users = HEntity.parse(hMessage.getPacket());
        if(users.length == 0)
            return;

        for(HEntity user: users) {
            int idx = user.getIndex();
            String name = user.getName();
            if(name.equals(chatty.getHabboInfo().getHabboName())){
                chatty.getHabboInfo().setIndex(idx);
                break;
            }
        }
    }


    public void respawnUserDummys() {
        for(Dummy d: dummys){
            spawnDummyInClient(d);
            sendMovePacket(d.id, d.x, d.y, DUMMYS_HEIGHT_OFFSET);
        }
    }

    private String fixEncoding(String text) {
        byte[] iso8859Bytes = text.getBytes(StandardCharsets.ISO_8859_1);
        return new String(iso8859Bytes, StandardCharsets.UTF_8);
    }


    public void sendChat(String habbo, Hotel hotel, String room, String msg, int style, boolean shout) {
        Dummy dummy = findDummy(room, habbo, hotel);
        if(dummy == null){
            System.out.println("No dummy of name " + habbo + " found");
            return;
        }
        String id = shout ? "Shout" : "Chat";
        chatty.sendToClient(new HPacket(id, HMessage.Direction.TOCLIENT, dummy.id, msg, 0, style, 0 , -1));
    }

    public void addDummy(String name, Hotel hotel, String room, String mission, String figure, String sex) {
        Dummy d = new Dummy(name, hotel, room, mission, figure, sex);
        if(!this.dummys.contains(d)) {
            this.dummys.add(d);
            spawnDummyInClient(d);
        }
    }

    public void spawnDummyInClient(Dummy d) {
        chatty.sendToClient(new HPacket("Users", HMessage.Direction.TOCLIENT, 1, d.id, d.getDetailedName(chatty.displayHotelEnabled()), d.mission,
                                               d.figure, d.id, 2000, 2000, "1.0", 2, 1, d.sex, -1, -1, 0, 849, false));
    }

    public void removeDummy(String name, String room) {
        for(int i = 0; i < dummys.size(); i++) {
            Dummy d = dummys.get(i);
            if(d.name.equals(name) && d.room.equals(room)) {
                chatty.sendToClient(new HPacket("UserRemove", HMessage.Direction.TOCLIENT, Integer.toString(d.id)));
                dummys.remove(i);
                return;
            }
        }
    }

    private Dummy findDummy(String room, String habbo, Hotel hotel) {
        for(Dummy d: dummys){
            if(d.name.equals(habbo) && d.room.equals(room) && d.hotel == hotel)
                return d;
        }
        return null;
    }

    public void clearAllDummys() {
        if(chatty.isGearthConnected()){
            for (Dummy d : dummys) {
                chatty.sendToClient(new HPacket("UserRemove", HMessage.Direction.TOCLIENT, Integer.toString(d.id)));
            }
        }
        dummys.clear();
    }

    public void sendInformationMsg(String msg) {
        if(chatty.sendInformationMsgEnabled())
            chatty.sendToClient(new HPacket("Whisper", HMessage.Direction.TOCLIENT, this.infoDummy.id, msg, 0, this.infoDummyChatBubble, 0, -1));
    }


    private void updateHabboPosition(HMessage hMessage) {
        //track the user positions to send the messages (speech bubbles) at the correct positions
        if(chatty == null || chatty.getHabboInfo() == null)
            return;

        HPacket packet = hMessage.getPacket();
        packet.readInteger();
        int index = packet.readInteger();
        if(index == chatty.getHabboInfo().getIndex()) {
            int x = packet.readInteger();
            int y = packet.readInteger();
            chatty.sendUserMoved(x, y);
        }
    }

    public void moveDummy(String habbo, Hotel hotel, String room, int x, int y) {
        Dummy d = findDummy(room, habbo, hotel);
        if(d != null){
            d.x = x;
            d.y = y;
            sendMovePacket(d.id, x, y, DUMMYS_HEIGHT_OFFSET);
        }

        HabboInfo self = chatty.getHabboInfo();
        if(self.getHotel() == hotel && self.getHabboName().equals(habbo)) {
            sendMovePacket(infoDummy.id, x, y, DUMMYS_HEIGHT_OFFSET);
        }
    }

    private void sendMovePacket(int dummyId, int x, int y, double z) {
        String height = Double.toString(z); //must be string
        HPacket mvPacket = new HPacket("UserUpdate", HMessage.Direction.TOCLIENT, 1, dummyId, x, y, height, 4, 4, "//");
        chatty.sendToClient(mvPacket);
    }

    public void setChatbubble(int id) {
        this.chatbubbleId = id;
    }

    private static class Dummy {

        private int id;
        private static int id_counter = -2;
        private String room;
        private String name;
        private String figure;
        private String sex;
        private String mission;
        private Hotel hotel;

        private int x;
        private int y;

        public Dummy(String name, Hotel hotel, String room, String mission, String figure, String sex) {
            this.id = id_counter;
            id_counter -= 1;
            this.name = name;
            this.hotel = hotel;
            this.room = room;
            this.figure = figure;
            this.sex = sex;
            this.mission = mission;
        }

        public String getDetailedName(boolean withHotel) {
            if(room == null)
                return name;
            if(hotel == null || !withHotel)
                return "["+room+"] " + name;

            return "["+hotel+"]["+room+"] " + name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Dummy dummy = (Dummy) o;
            return room.equals(dummy.room) &&
                    name.equals(dummy.name) &&
                    hotel == dummy.hotel;
        }

        @Override
        public int hashCode() {
            return Objects.hash(room, name, hotel);
        }
    }
}
