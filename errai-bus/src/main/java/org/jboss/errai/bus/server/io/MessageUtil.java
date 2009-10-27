package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityParts;

import java.util.Map;

public class MessageUtil {
    public static Map<String, Object> decodeToMap(String in) {
        //noinspection unchecked
        return (Map<String, Object>) new JSONDecoder(in).parse();
    }

    public static CommandMessage[] createCommandMessage(Object session, String json) {
        if (json.length() == 0) return new CommandMessage[0];
        String[] pkg = json.split("\\|\\|");
        CommandMessage[] c = new CommandMessage[pkg.length];

        for (int i = 0; i < pkg.length; i++) {
            Map<String, Object> parts = decodeToMap(pkg[i]);
            if (parts.containsKey(MessageParts.SessionID.name())) {
                // If the client is trying to send a session ID into the server bus, it might be trying to
                // do something evil.  So we check for, and remove it if this is the case.
                parts.remove(MessageParts.SessionID.name());
            }
            parts.put(SecurityParts.SessionData.name(), session);

            CommandMessage msg = CommandMessage.create().setParts(parts);

            if (parts.containsKey("__MarshalledTypes")) {
                TypeDemarshallHelper.demarshallAll((String) parts.get("__MarshalledTypes"), msg);
            }

            c[i] = msg;
        }

        return c;
    }
}
