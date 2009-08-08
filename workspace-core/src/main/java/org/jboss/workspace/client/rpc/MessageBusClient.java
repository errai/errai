package org.jboss.workspace.client.rpc;

import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.jboss.workspace.client.framework.AcceptsCallback;


public class MessageBusClient {
    private static List<AcceptsCallback> onSubscribeHooks = new ArrayList<AcceptsCallback>();

    public static void subscribe(String subject, Element scope, AcceptsCallback callback, Object subscriberData) {

        for (AcceptsCallback c : onSubscribeHooks) {
            c.callback(subject, subscriberData);
        }

        _subscribe(subject, scope, callback, subscriberData);
    }

    private native static void _subscribe(String subject, Element scope, AcceptsCallback callback,
                                        Object subscriberData) /*-{
         $wnd.PageBus.subscribe(subject, scope,
             function(subject, message, subscriberData) {
                callback.@org.jboss.workspace.client.framework.AcceptsCallback::callback(Ljava/lang/Object;Ljava/lang/Object;)(message, subscriberData)
             }, null);
    }-*/;

    public native static void store(String subject, Object value) /*-{
         $wnd.PageBus.store(subject, value);
    }-*/;


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


    public static String encodeMap(Map<String, Object> map) {
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

