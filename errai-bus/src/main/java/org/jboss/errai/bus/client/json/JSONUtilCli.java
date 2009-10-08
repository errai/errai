package org.jboss.errai.bus.client.json;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class JSONUtilCli {

    public static ArrayList<Message> decodePayload(Object value) {
        try {
            String str = String.valueOf(value);
            if (value == null || str.trim().length() == 0) return new ArrayList<Message>(0);

            ArrayList<Message> list = new ArrayList<Message>();

            System.out.println("decoding:" + str);

            JSONValue a = JSONParser.parse(str);

            if (a instanceof JSONArray) {
                JSONArray arr = (JSONArray) a;

                for (int i = 0; i < arr.size(); i++) {
                    a = arr.get(i);

                    if (a instanceof JSONObject) {
                        final JSONObject eMap = (JSONObject) a;
                        final String subject = eMap.keySet().iterator().next();

                        list.add(new Message() {
                            public String getSubject() {
                                return subject;
                            }

                            public Object getMessage() {
                                return eMap.get(subject);
                            }
                        });
                    }

                }
            }
            return list;
        }
        catch (Exception e) {
            System.err.println("Failed to decode payload:\n" + value);

            e.printStackTrace();
            return new ArrayList<Message>(0);
        }
    }

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
        else if (a != null) {
            throw new RuntimeException("bad encoding: " + a);
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
