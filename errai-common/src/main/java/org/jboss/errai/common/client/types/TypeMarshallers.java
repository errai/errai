/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.common.client.types;

import java.util.HashMap;
import java.util.Map;

public class TypeMarshallers {
  private static final Map<String, Marshaller<Object>> classMap = new HashMap<String, Marshaller<Object>>();
  private static final Map<Class, Marshaller<Object>> marshallers = new HashMap<Class, Marshaller<Object>>();

  static {
    addMarshaller(java.sql.Date.class, new Marshaller<java.sql.Date>() {
      public String marshall(java.sql.Date object, EncodingContext ctx) {
        return "{__EncodedType:\"java.sql.Date\", __ObjectID:\"" + object.hashCode() + "\", Value:" + object.getTime() + "}";
      }
    });

    addMarshaller(java.util.Date.class, new Marshaller<java.util.Date>() {
      public String marshall(java.util.Date object, EncodingContext ctx) {
        return "{__EncodedType:\"java.util.Date\", __ObjectID:\"" + object.hashCode() + "\", Value:" + object.getTime() + "}";
      }
    });

    addMarshaller(Byte.class, new Marshaller<Byte>() {
      public String marshall(Byte object, EncodingContext ctx) {
        return String.valueOf(object.intValue());
      }
    });

    addMarshaller(Character.class, new Marshaller<Character>() {
      public String marshall(Character object, EncodingContext ctx) {
        return "\"" + String.valueOf(object.charValue()) + "\"";
      }
    });
  }

  public static void addMarshaller(Class type, Marshaller d) {
    classMap.put(type.getName(), d);
    marshallers.put(type, d);
  }


//  public static <T> Marshaller<T> getMarshaller(Class<? extends T> type) {
//    return (Marshaller<T>) marshallers.get(type);
//  }
//
//  public static Marshaller<Object> getMarshaller(String type) {
//    return classMap.get(type);
//  }
//
//  public static boolean hasMarshaller(Class type) {
//    return marshallers.containsKey(type);
//  }
//
//  public static boolean hasMarshaller(String type) {
//    return classMap.containsKey(type);
//  }
}