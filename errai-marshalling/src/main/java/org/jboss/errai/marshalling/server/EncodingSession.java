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

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.AbstractMarshallingSession;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;

import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class EncodingSession extends AbstractMarshallingSession {
  private int escapeMode;

  @Override
  public Marshaller<Object, Object> getMarshallerForType(String fqcn) {
    return ServerTypeMarshallerFactory.getMarshaller(fqcn);
  }

  @Override
  public String marshall(Object o) {
    if (o == null) {
      return "null";
    }
    else {
      Marshaller<Object, Object> m = getMarshallerForType(o.getClass().getName());
      if (m == null) {
        throw new MarshallingException("no marshaller for type: " + o.getClass().getName());
      }
      return m.marshall(o, this);
    }
  }

  @Override
  public <T> T demarshall(Class<T> clazz, Object o) {
    if (o == null) {
      return null;
    }
    else {
      Marshaller<Object, Object> m = getMarshallerForType(clazz.getName());
      if (m == null) {
        throw new MarshallingException("no marshaller for type: " + o.getClass().getName());
      }
      return (T) m.demarshall(o, this);
    }
  }

  @Override
  public String determineTypeFor(String formatType, Object o) {
    JSONValue jsonValue = (JSONValue) o;

    if (jsonValue.isObject() != null) {
      JSONObject jsonObject = jsonValue.isObject();
      if (jsonObject.containsKey(SerializationParts.ENCODED_TYPE)) {
        return jsonObject.get(SerializationParts.ENCODED_TYPE).isString().stringValue();
      }
      else {
        return Map.class.getName();
      }
    }
    else if (jsonValue.isString() != null) {
      return String.class.getName();
    }
    else if (jsonValue.isNumber() != null) {
      return Double.class.getName();
    }
    else if (jsonValue.isBoolean() != null) {
      return Boolean.class.getName();
    }
    else if (jsonValue.isArray() != null) {
      return List.class.getName();
    }
    else if (jsonValue.isNull() != null) {
      return null;
    }
    throw new RuntimeException("unknown type: cannot reverse map value to concrete Java type: " + o);
  }


  public boolean isEncoded(Object ref) {
    return hasObjectHash(ref);
  }

  public String markReference(Object o) {
    if (o != null) {
      return getObjectHash(o);
    }
    return null;
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
