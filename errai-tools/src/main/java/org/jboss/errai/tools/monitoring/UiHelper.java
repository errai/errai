/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.tools.monitoring;

import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.marshalling.client.marshallers.ErraiProtocolEnvelopeMarshaller;
import org.jboss.errai.marshalling.server.DecodingSession;
import org.jboss.errai.marshalling.server.JSONDecoder;
import org.jboss.errai.marshalling.server.MappingContextSingleton;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.net.URL;
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

  public static Message uglyReEncode(String message) {
    if (message == null) return null;
    Map<String, Object> parts =
            ErraiProtocolEnvelopeMarshaller.INSTANCE.demarshall(JSONDecoder.decode(message),
                    new DecodingSession(MappingContextSingleton.get()));

    return CommandMessage.createWithParts(parts);
  }

  public static Message decodeAndDemarshall(String json) {
    Map<String, Object> parts = ErraiProtocolEnvelopeMarshaller.INSTANCE.demarshall(JSONDecoder.decode(json),
            new DecodingSession(MappingContextSingleton.get()));
    if (parts == null) return CommandMessage.create();
    return CommandMessage.createWithParts(parts);
  }
}
