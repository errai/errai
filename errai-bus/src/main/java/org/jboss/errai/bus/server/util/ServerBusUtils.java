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
import java.util.Set;

/**
 * The <tt>ServerBusUtils</tt> contains utilities for extracting information or converting information using
 * static functions defined throughout the server bus
 */
public class ServerBusUtils {
    public static void main(String[] args) {
        System.out.println("\"".replaceAll("\"", "\\\\\""));                                                       
    }

    /**
     * Decodes the object into a message object
     *
     * @param in - the object to decode
     * @return the <tt>Message</tt> instance
     */
    public static Message decodeToCommandMessage(Object in) {
        return CommandMessage.createWithParts(decodeMap(in));
    }

    /**
     * Decodes an object into a map, by determining the string value of the object. The string value of the object
     * should be in JSON format.
     *
     * @param value - the object to be decoded
     * @return an string->object map representing the object
     */
    public static Map<String, Object> decodeMap(Object value) {
        return MessageFactory.decodeToMap(String.valueOf(value));
    }

    /**
     * Encodes a given object into a JSON string
     *
     * @param value - the object to be encoded
     * @return a JSON string representing the object given
     */
    public static String encodeJSON(Object value) {
        return JSONEncoder.encode(value);
    }

    /**
     * Extracts the String-based SessionID which is used to identify the message queue associated with any particular
     * client. You may use this method to extract the SessionID from a message so that you may use it for routing.
     * 
     * @param message - the message to get the session id of
     * @return the string representation of the session id
     */
    public static String getSessionId(Message message) {
        return message.getResource(QueueSession.class, "Session").getSessionId();
    }
}
