package message;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChatMsg implements JSONSerializable {

    private String type;
    private ChatMsgData data;

    public ChatMsg(String type, Map<String, Object> obj) {
        this.type = type;
        this.data = new ChatMsgData(obj);
    }

    public ChatMsg(String type, ChatMsgData obj) {
        this.type = type;
        this.data = obj;
    }

    public ChatMsg(String type) {
        this.type = type;
    }


    public static ChatMsg parse(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);

        String type = jsonObject.getString("type");
        JSONObject json = jsonObject.getJSONObject("data");
        HashMap<String, Object> data = new HashMap<String, Object>();
        for(String key: json.keySet()) {
            data.put(key, json.get(key));
        }
        return new ChatMsg(type, data);
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", this.type);
        if(data != null) {
            JSONObject dataJson = new JSONObject();
            Map<String, Object> dataMap = data.serialize();
            for(String key: dataMap.keySet()) {
                dataJson.put(key, dataMap.get(key));
            }
            jsonObject.put("data", dataJson);
        }
        return jsonObject.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object>  obj = new HashMap<String, Object>();
        obj.put("type", type);
        if(data != null) {
            obj.put("data", data.serialize());
        }
        return obj;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ChatMsgData getData() {
        return data;
    }

    public void setData(ChatMsgData data) {
        this.data = data;
    }
}
