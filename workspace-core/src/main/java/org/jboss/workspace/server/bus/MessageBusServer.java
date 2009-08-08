package org.jboss.workspace.server.bus;

import com.google.gwt.user.client.Element;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.server.json.JSONUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageBusServer {
    private static List<AcceptsCallback> onSubscribeHooks = new ArrayList<AcceptsCallback>();

    public static void subscribe(String subject, Element scope, AcceptsCallback callback, Object subscriberData) {

        for (AcceptsCallback c : onSubscribeHooks) {
            c.callback(subject, subscriberData);
        }

        //      _subscribe(subject, scope, callback, subscriberData);
    }


    public static void store(String subject, Map<String, Object> value) {
        new SimpleMessageBusProvider().getBus().store(subject, encodeMap(value));
    }

    public static void store(String subject, Object value) {
        new SimpleMessageBusProvider().getBus().store(subject, value);
    }


    public static void addOnSubscribeHook(AcceptsCallback callback) {
        onSubscribeHooks.add(callback);
    }

    public static Map<String, Object> decodeMap(Object value) {
        return JSONUtil.decodeToMap(String.valueOf(value));
    }


    public static String encodeMap(Map<String, Object> map) {
        StringBuffer buf = new StringBuffer("{");
        Object v;

        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            buf.append("\"").append(entry.getKey()).append("\"").append(":");

            if ((v = entry.getValue()) == null) {
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
