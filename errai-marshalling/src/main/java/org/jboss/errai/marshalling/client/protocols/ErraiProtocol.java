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

package org.jboss.errai.marshalling.client.protocols;

import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.ErraiProtocolEnvelopeMarshaller;

import java.util.Map;

/**
 * @author Mike Brock
 */
public class ErraiProtocol {
  private static final ErraiProtocolEnvelopeMarshaller protocolMarshaller = new ErraiProtocolEnvelopeMarshaller();
  private static MarshallingSessionProvider sessionProvider;

  public static boolean isMarshallingSessionProviderRegistered() {
    return sessionProvider != null;
  }

  public static void setMarshallingSessionProvider(MarshallingSessionProvider provider) {
    if (sessionProvider != null) {
      throw new RuntimeException("session provider already set");
    }
    sessionProvider = provider;
  }

  public static String encodePayload(Map<String, Object> message) {
    return protocolMarshaller.marshall(message, sessionProvider.getEncoding());

  }

  public static Map<String, Object> decodePayload(EJValue value) {
    return protocolMarshaller.demarshall(value, sessionProvider.getDecoding());
  }
}
