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

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class MarshallUtil {
  private static final Set<Class<?>> primitiveWrappers = new HashSet<Class<?>>();

  static {
    primitiveWrappers.add(Integer.class);
    primitiveWrappers.add(Double.class);
    primitiveWrappers.add(Float.class);
    primitiveWrappers.add(Short.class);
    primitiveWrappers.add(Long.class);
    primitiveWrappers.add(Character.class);
    primitiveWrappers.add(Byte.class);
    primitiveWrappers.add(Boolean.class);
  }

  public static boolean isPrimitiveWrapper(Class<?> cls) {
    return primitiveWrappers.contains(cls);
  }

  public static <T> T notNull(String message, T obj) {
    if (obj == null) {
      throw new NullPointerException(message);
    }
    return obj;
  }

  public static boolean handles(EJValue object, Class<?> cls) {
    return handles(object.isObject(), cls);
  }

  public static boolean handles(EJObject object, Class<?> cls) {
    EJValue v = object.get(SerializationParts.ENCODED_TYPE);
    return !(v == null || v.isString() == null) && cls.getName().equals(v.isString().stringValue());
  }

  public static boolean handles(Map object, Class<?> cls) {
    String v = String.valueOf(object.get(SerializationParts.ENCODED_TYPE));
    return (v != null && cls.getName().equals(String.valueOf(v)));
  }


  public static Marshaller<Object> getQualifiedNumberMarshaller(Object o) {
    final Class<Object> type = (Class<Object>) o.getClass();

    return new Marshaller<Object>() {
      @Override
      public boolean handles(EJValue o) {
        return false;
      }

      @Override
      public String marshall(Object o, MarshallingSession ctx) {
        return NumbersUtils.qualifiedNumericEncoding(o);
      }

      @Override
      public Object demarshall(EJValue o, MarshallingSession ctx) {
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


  public static String jsonStringEscape(final String s) {
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      jsonStringEscape(sb, s.charAt(i));
    }
    return sb.toString();
  }

  public static String jsonStringEscape(final char ch) {
    StringBuilder sb = new StringBuilder(5);
    jsonStringEscape(sb, ch);
    return sb.toString();
  }

  public static void jsonStringEscape(StringBuilder sb, final char ch) {
    switch (ch) {
      case '"':
        sb.append("\\\"");
        break;
      case '\\':
        sb.append("\\\\");
        break;
      case '\b':
        sb.append("\\b");
        break;
      case '\f':
        sb.append("\\f");
        break;
      case '\n':
        sb.append("\\n");
        break;
      case '\r':
        sb.append("\\r");
        break;
      case '\t':
        sb.append("\\t");
        break;
      case '/':
        sb.append("\\/");
        break;
      default:
        if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F')
                || (ch >= '\u2000')) {

          String ss = Integer.toHexString(ch);
          sb.append("\\u");
          for (int k = 0; k < 4 - ss.length(); k++) {
            sb.append('0');
          }
          sb.append(ss.toUpperCase());
        }
        else {
          sb.append(ch);
        }
    }
  }
}
