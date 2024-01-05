package entities;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Chatroom {

    private String name;
    private String password;
    private boolean hasPassword;
    private Map<HabboId, HabboInfo> users;

    //for ui
    private boolean expanded;


    private Chatroom() {
        this.users = new HashMap<>();
        expanded = false;
    }

    public Chatroom(String name, boolean hasPassword) {
        this();
        this.name = name;
        this.hasPassword = hasPassword;
    }

    public Chatroom(String name, Map<HabboId, HabboInfo> users, boolean hasPassword) {
        this();
        this.name = name;
        this.users = users;
        this.hasPassword = hasPassword;
    }

    public void removeUser(String username, Hotel hotel) {
        HabboId id = new HabboId(username, hotel);
        HabboInfo habbo = users.get(id);
        if(habbo != null)
            users.remove(id);
    }

    public void addUser(HabboInfo habboInfo) {
        users.put(new HabboId(habboInfo.getHabboName(), habboInfo.getHotel()), habboInfo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chatroom chatroom = (Chatroom) o;
        return name.equals(chatroom.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean hasPassword() {
        return hasPassword;
    }

    public void setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    public Collection<HabboInfo> getUsers() {
        return users.values();
    }

    public void setUsers(Map<HabboId, HabboInfo> users) {
        this.users = users;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
