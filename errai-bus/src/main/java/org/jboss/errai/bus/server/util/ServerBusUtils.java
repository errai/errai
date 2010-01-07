/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.server.util;

import org.jboss.errai.bus.server.QueueSession;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.server.io.JSONEncoder;
import org.jboss.errai.bus.server.io.MessageFactory;

import java.util.Map;

public class ServerBusUtils {
    public static void main(String[] args) {
        System.out.println("\"".replaceAll("\"", "\\\\\""));                                                       
    }

    public static Message decodeToCommandMessage(Object in) {
        return CommandMessage.createWithParts(decodeMap(in));
    }

    public static Map<String, Object> decodeMap(Object value) {
        return MessageFactory.decodeToMap(String.valueOf(value));
    }

    public static String encodeJSON(Object value) {
        return JSONEncoder.encode(value);
    }

    public static String getSessionId(Message message) {
        return message.getResource(QueueSession.class, "Session").getSessionId();
    }
}
