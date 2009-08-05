package org.jboss.workspace.client.framework;

import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Element;

import java.util.HashMap;
import java.util.Map;


public class FederationUtil {
    public native static void subscribe(String subject, Element scope, AcceptsCallback callback,
                                        Object subscriberData) /*-{
         $wnd.PageBus.subscribe(subject, scope,
             function(subject, message, subscriberData) {
                callback.@org.jboss.workspace.client.framework.AcceptsCallback::callback(Ljava/lang/Object;Ljava/lang/Object;)(message, subscriberData)
             }, null);
    }-*/;

    public native static void store(String subject, Object value) /*-{
         $wnd.PageBus.store(subject, value);
    }-*/;


    public static Map<String, Object> decodeMap(Object value) {
        JSONValue a = JSONParser.parse(String.valueOf(value));

        Map<String, Object> m = new HashMap<String, Object>();

        if (a instanceof JSONArray) {
            JSONArray eMap = (JSONArray) a;
            JSONArray entry;
            JSONValue v;

            for (int i = 0; i < eMap.size(); i++) {
                if ((v = (entry = (JSONArray) eMap.get(i)).get(1).isString()) != null) {
                    m.put(entry.get(0).isString().stringValue(), ((JSONString) v).stringValue());
                }
                else if ((v = entry.get(1).isNumber()) != null) {
                    m.put(entry.get(0).isString().stringValue(), ((JSONNumber) v).doubleValue());
                }
                else if ((v = entry.get(1).isBoolean()) != null) {
                    m.put(entry.get(0).isString().stringValue(), ((JSONBoolean) v).booleanValue());
                }
            }

        }
        else {
            throw new RuntimeException("bad encoding");
        }

        return m;
    }


    public static String encodeMap(Map<String, Object> map) {
        JSONArray a = new JSONArray();
        Object v;
        JSONArray e;

        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            e = new JSONArray();
            e.set(0, new JSONString(entry.getKey()));

            v = entry.getValue();
            if (v == null) {
                e.set(1, JSONNull.getInstance());
            }
            else if (v instanceof String) {
                e.set(1, new JSONString((String) v));
            }
            else if (v instanceof Number) {
                e.set(1, new JSONNumber(((Number) v).doubleValue()));
            }
            else if (v instanceof Boolean) {
                e.set(1, JSONBoolean.getInstance((Boolean) v));
            }
            else {
                throw new RuntimeException("cannot encode element type: " + v);
            }

            a.set(i++, e);
        }

        return a.toString();
    }

}

