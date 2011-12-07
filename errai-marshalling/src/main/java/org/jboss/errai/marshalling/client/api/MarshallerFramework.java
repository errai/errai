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

package org.jboss.errai.marshalling.client.api;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.client.marshallers.MapMarshaller;
import org.jboss.errai.marshalling.client.marshallers.NullMarshaller;
import org.jboss.errai.marshalling.client.marshallers.ObjectMarshaller;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerFramework implements EntryPoint {
  private static MarshallerFactory marshallerFactory;

  static {
    marshallerFactory = GWT.create(MarshallerFactory.class);
  }

  @Override
  public void onModuleLoad() {
  }

  public static Object demarshalErraiJSON(JSONValue object) {
    JSONMarshallingSession session = new JSONMarshallingSession();

    Marshaller<Object, Object> marshaller =
            marshallerFactory.getMarshaller(null, session.determineTypeFor(null, object));

    if (marshaller == null) {
      throw new RuntimeException("no marshaller available for payload: " + session.determineTypeFor(null, object));
    }

    return marshaller.demarshall(object, session);
  }

  public static String marshalErraiJSON(Map<String, Object> map) {
    return new MapMarshaller().marshall(map, new JSONMarshallingSession());
  }

  public static String marshalErraiJSON(Object obj) {
    return new ObjectMarshaller().marshall(obj, new JSONMarshallingSession());
  }

  public static class JSONMarshallingSession extends AbstractMarshallingSession {

    private static final MappingContext mappingContext = new MappingContext() {
      @Override
      public Class<? extends Marshaller> getMarshallerClass(String clazz) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void registerMarshaller(String clazzName, Class<? extends Marshaller> clazz) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean hasMarshaller(String clazzName) {
        return marshallerFactory.getMarshaller(clazzName, "json") != null;
      }

      @Override
      public boolean canMarshal(String cls) {
        return marshallerFactory.getMarshaller("json", cls) != null;
      }

    };

    @Override
    public MappingContext getMappingContext() {
      return mappingContext;
    }

    @Override
    public Marshaller<Object, Object> getMarshallerInstance(String fqcn) {
      if (fqcn == null) {
        return NullMarshaller.INSTANCE;
      }

      return MarshallUtil.notNull("no marshaller for: " + fqcn, marshallerFactory.getMarshaller(null, fqcn));
    }

    @Override
    public String marshall(Object o) {
      if (o == null) {
        return "null";
      }
      else {
        Marshaller<Object, Object> m = getMarshallerInstance(o.getClass().getName());
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
        Marshaller<Object, Object> m = getMarshallerInstance(clazz.getName());
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
  }

}
