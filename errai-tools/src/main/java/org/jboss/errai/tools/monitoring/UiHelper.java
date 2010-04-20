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

package org.jboss.errai.tools.monitoring;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.server.io.JSONDecoder;
import org.jboss.errai.bus.server.io.JSONEncoder;
import org.jboss.errai.bus.server.io.TypeDemarshallHelper;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UiHelper {
    static {
        // force classloading of this class for usage in the static reference below.
        new UiHelper();
    }

    public static Icon getSwIcon(String name) {
        ClassLoader cls = UiHelper.class.getClassLoader();
        URL url = cls.getResource(name);

        if (url == null) throw new RuntimeException("could not find: " + name);

        return new ImageIcon(url);
    }

    public static DefaultMutableTreeNode createIconEntry(String icon, String name) {
        return new DefaultMutableTreeNode(new JLabel(name, getSwIcon(icon), SwingConstants.LEFT));
    }

    public static Message uglyReEncode(Message message) {
        Map<String, Object> parts = (Map<String, Object>) JSONDecoder.decode(JSONEncoder.encode(message.getParts()));

        Message newMessage = CommandMessage.createWithParts(parts);
        if (parts.containsKey("__MarshalledTypes")) {
            TypeDemarshallHelper.demarshallAll((String) parts.get("__MarshalledTypes"), newMessage);
        }
        return newMessage;
    }

    public static Message decodeAndDemarshall(String json) {
        Map<String, Object> parts = (Map<String, Object>) JSONDecoder.decode(json);

        if (parts == null) return CommandMessage.createWithParts(new HashMap());

        Message newMessage = CommandMessage.createWithParts(parts);
        if (parts.containsKey("__MarshalledTypes")) {
            TypeDemarshallHelper.demarshallAll((String) parts.get("__MarshalledTypes"), newMessage);
        }
        return newMessage;
    }
}
