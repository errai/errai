package org.jboss.errai.server.bus;

import org.jboss.errai.client.framework.AcceptsCallback;
import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.server.json.JSONUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageBusServer {
    private static List<AcceptsCallback> onSubscribeHooks = new ArrayList<AcceptsCallback>();

    public static void storeGlobal(String subject, CommandMessage message) {
        new SimpleMessageBusProvider().getBus().storeGlobal(subject, message);
    }

    public static void store(String subject, CommandMessage message) {
        try {
            new SimpleMessageBusProvider().getBus().store(subject, message);
        }
        catch (NoSubscribersToDeliverTo e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void store(String subject, CommandMessage message, boolean fireListeners) {
        try {
            new SimpleMessageBusProvider().getBus().store(subject, message, fireListeners);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addOnSubscribeHook(AcceptsCallback callback) {
        onSubscribeHooks.add(callback);
    }

    public static void addSubscribeListener(SubscribeListener listener) {
        new SimpleMessageBusProvider().getBus().addSubscribeListener(listener);
    }

    public static void addUnsubscribeListener(UnsubscribeListener listener) {
        new SimpleMessageBusProvider().getBus().addUnsubscribeListener(listener);
    }

    public static CommandMessage decodeToCommandMessage(Object in) {
        return new CommandMessage(decodeMap(in));
    }

    public static Map<String, Object> decodeMap(Object value) {
        return JSONUtil.decodeToMap(String.valueOf(value));
    }

    public static String encodeMap(Map<String, Object> map) {
        StringBuffer buf = new StringBuffer("{");
        Object v;

        int i = 0;
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if ((v = entry.getValue()) == null) {
                if (!first) {
                    buf.append(", ");
                }
                buf.append("\"").append(entry.getKey()).append("\"").append(":").append("null");
                first = false;

            }
            else if (v instanceof String) {
                if (!first) {
                    buf.append(", ");
                }
                buf.append("\"").append(entry.getKey()).append("\"").append(":").append("\"").append(v).append("\"");
                first = false;

            }
            else if (v instanceof Number) {
                if (!first) {
                    buf.append(", ");
                }
                buf.append("\"").append(entry.getKey()).append("\"").append(":").append(v);
                first = false;

            }
            else if (v instanceof Boolean) {
                if (!first) {
                    buf.append(", ");
                }
                buf.append("\"").append(entry.getKey()).append("\"").append(":").append(v);
                first = false;

            }
            else if (!(v instanceof Serializable)) {
                throw new RuntimeException("cannot encode element type: " + v);
            }
            i++;
        }
        buf.append("}");
        return buf.toString();
    }
}
