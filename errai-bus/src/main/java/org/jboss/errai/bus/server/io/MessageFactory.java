/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.json.EJArray;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.ErraiProtocolEnvelopeMarshaller;
import org.jboss.errai.marshalling.server.DecodingSession;
import org.jboss.errai.marshalling.server.JSONDecoder;
import org.jboss.errai.marshalling.server.JSONStreamDecoder;
import org.jboss.errai.marshalling.server.MappingContextSingleton;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.jboss.errai.bus.client.api.base.CommandMessage.createWithParts;
import static org.jboss.errai.bus.client.api.base.CommandMessage.createWithPartsFromRawMap;

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
    return (Map<String, Object>) JSONDecoder.decode(in);
  }

  /**
   * Creates the command message from the given JSON string and session. The message is constructed in
   * parts depending on the string
   *
   * @param session - the queue session in which the message exists
   * @param request -
   * @param json    - the string representing the parts of the message
   * @return the message array constructed using the JSON string
   */
  public static Message createCommandMessage(QueueSession session, HttpServletRequest request, String json) {
    if (json.length() == 0) return null;

    Map<String, Object> parts = decodeToMap(json);
    parts.remove(MessageParts.SessionID.name());

    return from(parts, session, request);
  }

  @SuppressWarnings("unchecked")
  public static Message createCommandMessage(QueueSession session, String json) {
    if (json.length() == 0) return null;

    Message msg = createWithPartsFromRawMap(ErraiProtocolEnvelopeMarshaller.INSTANCE.demarshall(JSONDecoder.decode(json),
        new DecodingSession(MappingContextSingleton.get())))
            .setResource("Session", session)
            .setResource("SessionID", session.getSessionId());

    msg.setFlag(RoutingFlag.FromRemote);

    return msg;
  }


  public static List<Message> createCommandMessage(QueueSession session, HttpServletRequest request) throws IOException {
    EJValue value = JSONStreamDecoder.decode(request.getInputStream());
    if (value.isObject() != null) {
      return Collections.singletonList(from(getParts(value), session, request));
    }
    else if (value.isArray() != null) {
      EJArray arr = value.isArray();
      List<Message> messages = new ArrayList<Message>(arr.size());
      for (int i = 0; i < arr.size(); i++) {
        messages.add(from(getParts(arr.get(i)), session, request));
      }
      return messages;
    }
    else if (value.isNull()) {
      return Collections.<Message>emptyList();
    }
    else {
      throw new RuntimeException("bad payload");
    }
  }


  public static List<Message> createCommandMessage(QueueSession session, InputStream inputStream) throws IOException {
    EJValue value = JSONStreamDecoder.decode(inputStream);
    if (value.isObject() != null) {
      return Collections.singletonList(from(getParts(value), session, null));
    }
    else if (value.isArray() != null) {
      EJArray arr = value.isArray();
      List<Message> messages = new ArrayList<Message>(arr.size());
      for (int i = 0; i < arr.size(); i++) {
        messages.add(from(getParts(arr.get(i)), session, null));
      }
      return messages;
    }
    else {
      throw new RuntimeException("bad payload");
    }
  }

  public static List<Message> createCommandMessage(QueueSession session, EJValue value) {
    if (value.isObject() != null) {
      return Collections.singletonList(from(getParts(value), session, null));
    }
    else if (value.isArray() != null) {
      EJArray arr = value.isArray();
      List<Message> messages = new ArrayList<Message>(arr.size());
      for (int i = 0; i < arr.size(); i++) {
        messages.add(from(getParts(arr.get(i)), session, null));
      }
      return messages;
    }
    else {
      throw new RuntimeException("bad payload");
    }
  }



  private static Map getParts(EJValue value) {
    return ErraiProtocolEnvelopeMarshaller.INSTANCE.demarshall(value,
            new DecodingSession(MappingContextSingleton.get()));
  }

  @SuppressWarnings("unchecked")
  private static Message from(Map parts, QueueSession session, HttpServletRequest request) {
    Message msg = createWithParts(parts)
            .setResource("Session", session)
            .setResource("SessionID", session.getSessionId())
            .setResource(HttpServletRequest.class.getName(), request);
    msg.setFlag(RoutingFlag.FromRemote);
    return msg;
  }
}
