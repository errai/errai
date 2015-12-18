/*
 * Copyright (C) 2010 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.HttpSessionProvider.SessionsContainer;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * Attempts to serialize all attributes of the current HttpSession each time called.
 */
@Service
public class SessionPassivationService implements MessageCallback {
  @Override
  public void callback(Message message) {
    System.out.println("Attempting to serialize the session...");
    HttpServletRequest request = message.getResource(HttpServletRequest.class, HttpServletRequest.class.getName());
    HttpSession session = request.getSession();
    for (Enumeration<String> names = session.getAttributeNames(); names.hasMoreElements(); ) {
      String name = names.nextElement();
      Object attribute = session.getAttribute(name);
      Object deserializedAttribute = null;
      System.out.println("Session attribute: " + name);
      try {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ObjectOutputStream(bytes).writeObject(attribute);
        deserializedAttribute = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
      }
      catch (Exception ex) {
        ex.printStackTrace();
        throw new RuntimeException("Session is not serializable! Offending attribute: " + name);
      }
      if (attribute instanceof HttpSession) {
        throw new RuntimeException("The session contains an HttpSession! This is not allowed!");
      }
      if (attribute instanceof SessionsContainer) {
        // this could cause a NPE if session doesn't deserialize properly
        ((SessionsContainer) deserializedAttribute).getSession("doesn't matter");
      }
    }
    MessageBuilder.createConversation(message)
        .subjectProvided()
        .done().reply();
  }
}
