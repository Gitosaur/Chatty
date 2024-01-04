package message;

import java.util.HashMap;
import java.util.Map;

public class ChatMsgData implements JSONSerializable {

    private Map<String, Object> data;

    public ChatMsgData() {
        this.data = new HashMap<String, Object>();
    }

    public ChatMsgData(Map<String, Object> values) {
        this();
        this.data = values;
    }

    public void put(String key, Object value) {
        this.data.put(key, value);
    }

    @Override
    public Map<String, Object> serialize() {
        return data;
    }


    public Object get(String key) {
        return this.data.get(key);
    }


}
