package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.json.JSONEncoder;
import org.jboss.errai.bus.server.json.JSONUtil;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Map;

public class MessageBusServer {
    public static void main(String[] args) {
        System.out.println("\"".replaceAll("\"", "\\\\\""));
    }

    public static CommandMessage decodeToCommandMessage(Object in) {
        return new CommandMessage(decodeMap(in));
    }

    public static Map<String, Object> decodeMap(Object value) {
        return JSONUtil.decodeToMap(String.valueOf(value));
    }

    public static String encodeJSON(Object value) {
        return new JSONEncoder().encode(value);
    }

    public static String getSessionId(CommandMessage message) {
        return (String) message.get(HttpSession.class, SecurityParts.SessionData).getAttribute(MessageBus.WS_SESSION_ID);
    }
}
