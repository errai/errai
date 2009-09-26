package org.jboss.errai.server.bus;

import org.jboss.errai.client.framework.AcceptsCallback;
import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.protocols.MessageParts;
import org.jboss.errai.client.rpc.protocols.SecurityParts;
import org.jboss.errai.server.json.JSONUtil;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageBusServer {
    private static List<AcceptsCallback> onSubscribeHooks = new ArrayList<AcceptsCallback>();

    public static void sendGlobal(String subject, CommandMessage message) {
        new DefaultMessageBusProvider().getBus().sendGlobal(subject, message);
    }

    public static void send(String sessionId, String subject, CommandMessage message) {
        try {
            new DefaultMessageBusProvider().getBus().send(sessionId, subject, message);
        }
        catch (NoSubscribersToDeliverTo e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(String subject, CommandMessage message) {
        try {
            new DefaultMessageBusProvider().getBus().send(subject, message);
        }
        catch (NoSubscribersToDeliverTo e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(String subject, CommandMessage message, boolean fireListeners) {
        try {
            new DefaultMessageBusProvider().getBus().send(subject, message, fireListeners);
        }
        catch (NoSubscribersToDeliverTo e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(CommandMessage message) {
        if (message.hasPart(MessageParts.ToSubject)) {
            send(message.get(String.class, MessageParts.ToSubject), message);
        }
        else {
            throw new RuntimeException("Cannot send message using this method if the message does not contain a ToSubject field.");
        }
    }

    public static void addOnSubscribeHook(AcceptsCallback callback) {
        onSubscribeHooks.add(callback);
    }

    public static void addSubscribeListener(SubscribeListener listener) {
        new DefaultMessageBusProvider().getBus().addSubscribeListener(listener);
    }

    public static void addUnsubscribeListener(UnsubscribeListener listener) {
        new DefaultMessageBusProvider().getBus().addUnsubscribeListener(listener);
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

    public static String getSessionId(CommandMessage message) {
        return (String) message.get(HttpSession.class, SecurityParts.SessionData).getAttribute(MessageBus.WS_SESSION_ID);
    }
}
