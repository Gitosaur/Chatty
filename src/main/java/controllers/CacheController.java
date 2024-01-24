package controllers;

import gearth.misc.Cacher;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;

public class CacheController {

    private Chatty chatty;
    private JSONObject cacheContents;

    public CacheController(Chatty chatty) {
        this.chatty = chatty;
        setupCache();
    }


    private void setupCache() {
        File extDir = null;
        try {
            extDir = (new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI())).getParentFile();
            if (extDir.getName().toLowerCase().equals("extensions")) {
                extDir = extDir.getParentFile();
            }
        } catch (URISyntaxException ignored) {}
        Cacher.setCacheDir(extDir + File.separator + "Cache");
        this.cacheContents = Cacher.getCacheContents();
    }

    private void loadCache() {

//        chatty server url

//        active chatbubble

//        toggle shortcut

//        receive chat info in client

//        show hotels in client

//        typing indicator setting

//        always on top
    }

    public boolean has(String key) {
        return cacheContents.has(key);
    }

    public void put(String key, Object val) {
        Cacher.put(key, val);
    }

    public boolean optBool(String key, boolean defaultValue) {
        return cacheContents.optBoolean(key, defaultValue);
    }

    public String optString(String key, String defaultValue) {
        return cacheContents.optString(key, defaultValue);
    }

    public int getInt(String key) {
        return cacheContents.getInt(key);
    }

    public void remove(String key) {
        Cacher.remove(key);
    }
}
