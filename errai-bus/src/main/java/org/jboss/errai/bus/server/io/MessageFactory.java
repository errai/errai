/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.api.QueueSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.jboss.errai.bus.client.api.base.CommandMessage.createWithParts;

/**
 * The <tt>MessageFactory</tt> facilitates the building of a command message using a JSON string
 */
public class MessageFactory {

    /**
     * Decodes a JSON string to a map (string name -> object)
     *
     * @param in - JSON string
     * @return map representing the string
     */
    public static Map<String, Object> decodeToMap(String in) {
        //noinspection unchecked
        return (Map<String, Object>) new JSONDecoder(in).parse();
    }

    /**
     * Creates the command message from the given JSON string and session. The message is constructed in
     * parts depending on the string
     *
     * @param session - the queue session in which the message exists
     * @param json    - the string representing the parts of the message
     * @return the message array constructed using the JSON string
     */
    public static Message createCommandMessage(QueueSession session, String json) {
        if (json.length() == 0) return null;

        Map<String, Object> parts = decodeToMap(json);
        parts.remove(MessageParts.SessionID.name());

        Message msg = createWithParts(parts)
                .setResource("Session", session);

        // experimental feature. does this need to be cleaned?
        // any chance this leaks the CL?
        //   msg.setResource("errai.experimental.classLoader", classLoader);

        msg.setFlag(RoutingFlags.FromRemote);

        return msg;

    }

    public static Message createCommandMessage(QueueSession session, InputStream stream) throws IOException {
        Map<String, Object> parts = (Map<String, Object>) JSONStreamDecoder.decode(stream);
        parts.remove(MessageParts.SessionID.name());

        Message msg = createWithParts(parts)
                .setResource("Session", session);

        // experimental feature. does this need to be cleaned?
        // any chance this leaks the CL?
        //   msg.setResource("errai.experimental.classLoader", classLoader);

        msg.setFlag(RoutingFlags.FromRemote);

        return msg;
    }
}
