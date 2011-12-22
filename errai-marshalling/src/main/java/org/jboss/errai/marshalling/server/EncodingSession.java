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

package org.jboss.errai.marshalling.server;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.AbstractMarshallingSession;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;

import java.util.Map;

/**
 * @author Mike Brock
 */
public class EncodingSession extends AbstractMarshallingSession {
  private int escapeMode;
  private final ServerMappingContext context;

  public EncodingSession(ServerMappingContext context) {
    this.context = context;
  }

  @Override
  public String marshall(Object o) {
    if (o == null) {
      return "null";
    }
    else {
      Marshaller<Object> m = getMarshallerInstance(o.getClass().getName());
      if (m == null) {
        throw new MarshallingException("no marshaller for type: " + o.getClass().getName());
      }
      return m.marshall(o, this);
    }
  }

  @Override
  public <T> T demarshall(Class<T> clazz, EJValue o) {
    if (o == null) {
      return null;
    }
    else {
      Marshaller<Object> m = getMarshallerInstance(clazz.getName());
      if (m == null) {
        throw new MarshallingException("no marshaller for type: " + o.getClass().getName());
      }
      return (T) m.demarshall(o, this);
    }
  }

  @Override
  public String determineTypeFor(String formatType, Object o) {
    if (o == null) return null;

    if (o instanceof Map) {
      Map map = (Map) o;
      if (map.containsKey(SerializationParts.ENCODED_TYPE)) {
        return (String) map.get(SerializationParts.ENCODED_TYPE);
      }
      else {
        return Map.class.getName();
      }
    }
    else {
      return o.getClass().getName();
    }
  }

  @Override
  public Marshaller<Object> getMarshallerInstance(String fqcn) {
    MappingDefinition md = context.getDefinitionsFactory().getDefinition(fqcn);
    if (md == null) {
      throw new RuntimeException("no marshaller available for: " + fqcn);
    }
    return md.getMarshallerInstance();
  }

  @Override
  public ServerMappingContext getMappingContext() {
    return context;
  }

  public boolean isEscapeMode() {
    return escapeMode != 0;
  }

  public void setEscapeMode() {
    escapeMode++;
  }

  public void unsetEscapeMode() {
    escapeMode--;
  }
}
