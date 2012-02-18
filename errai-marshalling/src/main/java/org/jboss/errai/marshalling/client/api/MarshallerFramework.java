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
import com.google.gwt.json.client.JSONParser;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.api.json.impl.gwt.GWTJSON;
import org.jboss.errai.marshalling.client.protocols.MarshallingSessionProvider;

import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallerFramework implements EntryPoint {
  private static MarshallerFactory marshallerFactory;
  private static boolean automarshal = true;

  static {
    InitVotes.waitFor(MarshallerFramework.class);
    marshallerFactory = GWT.create(MarshallerFactory.class);

    ParserFactory.registerParser(new Parser() {
      @Override
      public EJValue parse(String input) {
        return GWTJSON.wrap(JSONParser.parseStrict(input));
      }
    });

    InitVotes.voteFor(MarshallerFramework.class);
  }


  @Override
  public void onModuleLoad() {
  }

  public static void initializeDefaultSessionProvider() {
    if (!MarshallingSessionProviderFactory.isMarshallingSessionProviderRegistered()) {
      MarshallingSessionProviderFactory.setMarshallingSessionProvider(new MarshallingSessionProvider() {
        @Override
        public MarshallingSession getEncoding() {
          return new MarshallerFramework.JSONMarshallingSession();
        }

        @Override
        public MarshallingSession getDecoding() {
          return new MarshallerFramework.JSONMarshallingSession();
        }

        @Override
        public boolean hasMarshaller(String fqcn) {
          return MarshallerFramework.getMarshallerFactory().getMarshaller(null, fqcn) != null;
        }

        @Override
        public Marshaller getMarshaller(String fqcn) {
          return MarshallerFramework.getMarshallerFactory().getMarshaller(null, fqcn);
        }
      });
    }
  }


  public static class JSONMarshallingSession extends AbstractMarshallingSession {

    private static final MappingContext mappingContext = new MappingContext() {
      @Override
      public Marshaller<Object> getMarshaller(String clazz) {
        return marshallerFactory.getMarshaller("json", clazz);
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
    public Marshaller<Object> getMarshallerInstance(String fqcn) {
      if (automarshal) {
        return marshallerFactory.getMarshaller(null, fqcn);
      }
      else {
        Marshaller<Object> m = marshallerFactory.getMarshaller(null, fqcn);
        if (m == null) {
          m = new Marshaller<Object>() {
            @Override
            public Class<Object> getTypeHandled() {
              return Object.class;
            }

            @Override
            public String getEncodingType() {
              return "json";
            }

            @Override
            public Object demarshall(EJValue o, MarshallingSession ctx) {
              return o.getRawValue().toString();
            }

            @Override
            public String marshall(Object o, MarshallingSession ctx) {
              return "\"MarshallingNotSupported\"";
            }

            @Override
            public boolean handles(EJValue o) {
              return false;
            }
          };
        }
        return m;
      }
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
          throw new MarshallingException("no marshaller for type: " + clazz.getName());
        }
        return (T) m.demarshall(o, this);
      }
    }

    @Override
    public String determineTypeFor(String formatType, Object o) {
      EJValue jsonValue = (EJValue) o;

      if (jsonValue.isObject() != null) {
        EJObject jsonObject = jsonValue.isObject();
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

  public static void setAutomarshal(boolean auto) {
    automarshal = auto;
  }

  public static boolean isAutomarshal() {
    return automarshal;
  }

  public static MarshallerFactory getMarshallerFactory() {
    return marshallerFactory;
  }
}
