package org.jboss.errai.client.rpc.json;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.client.rpc.CommandMessage;

import java.util.HashMap;
import java.util.Map;


public class JSONUtilCli {
    public static Map<String, Object> decodeMap(Object value) {
        JSONValue a = JSONParser.parse(String.valueOf(value));

        Map<String, Object> m = new HashMap<String, Object>();

        if (a instanceof JSONObject) {
            JSONObject eMap = (JSONObject) a;

            for (String key : eMap.keySet()) {
                JSONValue v = eMap.get(key);

                if (v.isString() != null) {
                    m.put(key, v.isString().stringValue());
                }
                else if (v.isNumber() != null) {
                    m.put(key, v.isNumber().doubleValue());
                }
                else if (v.isBoolean() != null) {
                    m.put(key, v.isBoolean().booleanValue());
                }
                else if (v.isNull() != null) {
                    m.put(key, null);
                }
            }
        }
        else {
            throw new RuntimeException("bad encoding");
        }

        return m;
    }

    public static CommandMessage decodeCommandMessage(Object value) {
        return new CommandMessage(decodeMap(value));
    }

    public static String encodeMap(Map<String, Object> map) {
        if (map.size() == 0) return "{}";

        StringBuffer buf = new StringBuffer("{");
        Object v;

        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {

            buf.append("\"").append(entry.getKey()).append("\"").append(":");

            v = entry.getValue();
            if (v == null) {
                buf.append("null");
            }
            else if (v instanceof String) {
                buf.append("\"").append(v).append("\"");
            }
            else if (v instanceof Number) {
                buf.append(v);
            }
            else if (v instanceof Boolean) {
                buf.append(v);
            }
            else {
                throw new RuntimeException("cannot encode element type: " + v);
            }

            if (++i < map.size()) buf.append(", ");
        }


        return buf.append("}").toString();
    }

}
