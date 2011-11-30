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

package org.jboss.errai.marshalling.client.util;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.common.client.types.EncodingContext;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class MarshallUtil {
  public static <T> T notNull(String message, T obj) {
    if (obj == null) {
      throw new NullPointerException(message);
    }
    return obj;
  }

  public static <T> T demarshallCache(Class<T> type, JSONObject jsonObject, MarshallingSession session) {
    final String hashCode = jsonObject.get(SerializationParts.OBJECT_ID).isNumber().toString();

    if (session.hasObjectHash(hashCode)) {
      return session.getObject(type, hashCode);
    }

    final String typeName = jsonObject.get(SerializationParts.ENCODED_TYPE).isString().stringValue();
    final Object demarshalledInstance = session.getMarshallerForType(typeName).demarshall(jsonObject, session);
    session.recordObjectHash(hashCode, demarshalledInstance);

    return (T) demarshalledInstance;
  }

  public static <T> T demarshallCache(Class<T> type, JSONObject jsonObject, MarshallingSession session,
                                      Marshaller<Object, T> marshaller) {
    final String hashCode = jsonObject.get(SerializationParts.OBJECT_ID).isNumber().toString();

    if (session.hasObjectHash(hashCode)) {
      return session.getObject(type, hashCode);
    }

    final T demarshalledInstance = marshaller.demarshall(jsonObject, session);
    session.recordObjectHash(hashCode, demarshalledInstance);

    return demarshalledInstance;
  }

  public static JSONValue nullSafe_JSONObject(JSONValue v, String key) {
    if (v == null || v.isObject() == null) {
      return null;
    }
    else {
      return v.isObject().get(key);
    }
  }

  public static <T extends Enum<T>> T demarshalEnum(Class<T> enumType, JSONObject obj, String name) {
    if (obj == null || !obj.containsKey(name) || obj.get(name).isNull() != null) {
      return null;
    }
    return Enum.valueOf(enumType, obj.get(name).isString().stringValue());
  }

  public static boolean handles(JSONObject object, Class<?> cls) {
    JSONValue v = object.get(SerializationParts.ENCODED_TYPE);
    return !(v == null || v.isString() == null) && cls.getName().equals(v.isString().stringValue());
  }

  public static Marshaller<Object, Object> getQualifiedNumberMarshaller(Object o) {
      final Class<Object> type = (Class<Object>) o.getClass();

      return new Marshaller<Object, Object>() {
        @Override
        public boolean handles(Object o) {
          return false;
        }

        @Override
        public String marshall(Object o, MarshallingSession ctx) {
          return NumbersUtils.qualifiedNumericEncoding(false, o);
        }

        @Override
        public Object demarshall(Object o, MarshallingSession ctx) {
          return null;
        }

        @Override
        public String getEncodingType() {
          return "json";
        }

        @Override
        public Class<Object> getTypeHandled() {
          return type;
        }
      };

  }


}
