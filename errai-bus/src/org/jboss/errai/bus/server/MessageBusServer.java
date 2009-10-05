package org.jboss.errai.bus.server;

import org.jboss.errai.workspaces.client.bus.CommandMessage;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.json.JSONUtil;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Map;

public class MessageBusServer {


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
