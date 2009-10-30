package org.jboss.errai.bus.server.util;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.io.JSONEncoder;
import org.jboss.errai.bus.server.io.MessageUtil;

import javax.servlet.http.HttpSession;
import java.util.Map;

public class ServerBusUtils {
    public static void main(String[] args) {
        System.out.println("\"".replaceAll("\"", "\\\\\""));                                                       
    }

    public static CommandMessage decodeToCommandMessage(Object in) {
        return new CommandMessage(decodeMap(in));
    }

    public static Map<String, Object> decodeMap(Object value) {
        return MessageUtil.decodeToMap(String.valueOf(value));
    }

    public static String encodeJSON(Object value) {
        return new JSONEncoder().encode(value);
    }

    public static String getSessionId(CommandMessage message) {
        return (String) ((HttpSession) message.getResource("Session")).getAttribute(MessageBus.WS_SESSION_ID);
    }
}
