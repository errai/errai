package org.jboss.workspace.client.rpc;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.framework.MessageCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageBusClient {
    private static List<AcceptsCallback> onSubscribeHooks = new ArrayList<AcceptsCallback>();

    public static void subscribe(String subject, MessageCallback callback, Object subscriberData) {

        for (AcceptsCallback c : onSubscribeHooks) {
            c.callback(subject, subscriberData);
        }

        _subscribe(subject, callback, subscriberData);
    }

    public static void subscribe(String subject, MessageCallback callback) {

        for (AcceptsCallback c : onSubscribeHooks) {
            c.callback(subject, null);
        }

        _subscribe(subject, callback, null);
    }

    private native static void _subscribe(String subject, MessageCallback callback,
                                          Object subscriberData) /*-{

         $wnd.PageBus.subscribe(subject, null,
                 function (subject, message, subcriberData) {
                    callback.@org.jboss.workspace.client.framework.MessageCallback::callback(Lorg/jboss/workspace/client/rpc/CommandMessage;)(@org.jboss.workspace.client.rpc.MessageBusClient::decodeCommandMessage(Ljava/lang/Object;)(message))
                 },
                 null);

  
    }-*/;

    public native static void store(String subject, Object value) /*-{
         $wnd.PageBus.store(subject, value);
    }-*/;

    public static void store(String subject, Map<String, Object> message) {
        store(subject, encodeMap(message));
    }

    public static void store(String subject, CommandMessage message) {
        store(subject, message.getParts());
    }

    public static void addOnSubscribeHook(AcceptsCallback callback) {
        onSubscribeHooks.add(callback);
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
        else {
            throw new RuntimeException("bad encoding");
        }

        return m;
    }


    public static CommandMessage decodeCommandMessage(Object value) {
        return new CommandMessage(decodeMap(value), String.valueOf(value));
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

