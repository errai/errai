/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client.util;

import org.jboss.errai.bus.client.api.HasEncoded;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.marshalling.client.protocols.ErraiProtocol;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class BusTools {
  public static final Set<String> RESERVED_SERVICES;

  static {
    final Set<String> s = new HashSet<String>();

    s.add("ServerBus");
    s.add("AuthorizationService");
    s.add("AuthenticationService");
    s.add("ServerEchoService");

    s.add("ClientBus");
    s.add(DefaultErrorCallback.CLIENT_ERROR_SUBJECT);

    RESERVED_SERVICES = Collections.unmodifiableSet(s);
  }

  public static boolean isReservedName(String name) {
    return RESERVED_SERVICES.contains(name);
  }
  
  public static String encodeMessage(Message message) {
    if (message instanceof HasEncoded) {
      return ((HasEncoded) message).getEncoded();
    }
    else {
      return ErraiProtocol.encodePayload(message.getParts());
    }
  }
  
  public static String encodeMessages(Queue<Message> messages) {
    StringBuilder sbuf = new StringBuilder("[");
    boolean first = true;
    Message m;
    while ((m = messages.poll()) != null) {
      if (!first) {
        sbuf.append(',');
      }
      sbuf.append(encodeMessage(m));
      first = false;
    }
    return sbuf.append("]").toString();
  }
}
