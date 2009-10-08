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
        return (Map<String, Object>) new JSONDecoderCli().decode(value);
    }

    public static CommandMessage decodeCommandMessage(Object value) {
        return new CommandMessage(decodeMap(value));
    }

    public static String encodeMap(Map<String, Object> map) {
        return new JSONEncoderCli().encode(map);
    }

}
