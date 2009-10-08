package org.jboss.errai.bus.client.json;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JSONDecoderCli {

    public Object decode(Object value) {
        JSONValue v = JSONParser.parse(String.valueOf(value));

        return _decode(v);
    }

    private Object _decode(JSONValue v) {
        if (v.isString() != null) {
            return v.isString().stringValue();
        } else if (v.isNumber() != null) {
            return v.isNumber().doubleValue();
        } else if (v.isBoolean() != null) {
            return v.isBoolean().booleanValue();
        } else if (v.isNull() != null) {
            return null;
        } else if (v instanceof JSONObject) {
            return decodeObject(v.isObject());
        } else if (v instanceof JSONArray) {
            return decodeList(v.isArray());
        }
        else {
            throw new RuntimeException("unknown encoding");
        }

    }

    private Map<String, Object> decodeObject(JSONObject eMap) {
        Map<String, Object> m = new HashMap<String, Object>();

        for (String key : eMap.keySet()) {            
            m.put(key, _decode(eMap.get(key)));
        }

        return m;
    }

    private List<Object> decodeList(JSONArray arr) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < arr.size(); i++) {
            list.add(_decode(arr.get(i)));
        }
        return list;
    }

}
