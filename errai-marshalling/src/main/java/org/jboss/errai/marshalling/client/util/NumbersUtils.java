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

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class NumbersUtils {
  public static Object getNumber(String wrapperClassName, JSONValue value) {
    if (Integer.class.getName().equals(wrapperClassName)) {
      return new Double(value.isNumber().doubleValue()).intValue();
    }
    else if (Double.class.getName().equals(wrapperClassName)) {
      return value.isNumber().doubleValue();
    }
    else if (Long.class.getName().equals(wrapperClassName)) {
      return Long.parseLong(value.isString().stringValue());
    }
    else if (Boolean.class.getName().equals(wrapperClassName)) {
      return value.isBoolean().booleanValue();
    }
    else if (Float.class.getName().equals(wrapperClassName)) {
      return new Double(value.isNumber().doubleValue()).floatValue();
    }
    else if (Short.class.getName().equals(wrapperClassName)) {
      return new Double(value.isNumber().doubleValue()).shortValue();
    }
    else if (Character.class.getName().equals(wrapperClassName)) {
      return value.isString().stringValue().charAt(0);
    }
    else if (Byte.class.getName().equals(wrapperClassName)) {
      return new Double(value.isNumber().doubleValue()).byteValue();
    }
    else {
      throw new RuntimeException("unknown type");
    }
  }

  public static Object getNumber(String wrapperClassName, Object value) {
    if (value instanceof String) {
      return getNumber(wrapperClassName, (String) value);
    }
    else if (value instanceof Double) {
      return getNumber(wrapperClassName, (Double) value);
    }
    else if (value instanceof Boolean) {
      return value;
    }
    else if (value instanceof Long) {
      return getNumber(wrapperClassName, (Long) value);
    }
 
    else {
      throw new RuntimeException("unknown numeric type: " + value);
    }
  }

  private static Object getNumber(String wrapperClassName, String value) {
    if (Integer.class.getName().equals(wrapperClassName)) {
      return Integer.parseInt(value);
    }
    else if (Double.class.getName().equals(wrapperClassName)) {
      return Double.parseDouble(value);
    }
    else if (Long.class.getName().equals(wrapperClassName)) {
      return Long.parseLong(value);
    }
    else if (Boolean.class.getName().equals(wrapperClassName)) {
      return Boolean.parseBoolean(value);
    }
    else if (Float.class.getName().equals(wrapperClassName)) {
      return Float.parseFloat(value);
    }
    else if (Short.class.getName().equals(wrapperClassName)) {
      return Short.parseShort(value);
    }
    else if (Character.class.getName().equals(wrapperClassName)) {
      return value.charAt(0);
    }
    else if (Byte.class.getName().equals(wrapperClassName)) {
      return Byte.parseByte(value);
    }
    else {
      throw new RuntimeException("unknown type");
    }
  }

  private static Object getNumber(String wrapperClassName, Double value) {
    if (Integer.class.getName().equals(wrapperClassName)) {
      return value.intValue();
    }
    else if (Double.class.getName().equals(wrapperClassName)) {
      return value;
    }
    else if (Long.class.getName().equals(wrapperClassName)) {
      return value.longValue();
    }
    else if (Boolean.class.getName().equals(wrapperClassName)) {
      return value.intValue() != 0;
    }
    else if (Float.class.getName().equals(wrapperClassName)) {
      return value.floatValue();
    }
    else if (Short.class.getName().equals(wrapperClassName)) {
      return value.shortValue();
    }
    else if (Byte.class.getName().equals(wrapperClassName)) {
      return value.byteValue();
    }
    else {
      throw new RuntimeException("unknown type");
    }
  }

  private static Object getNumber(String wrapperClassName, Long value) {
    if (Integer.class.getName().equals(wrapperClassName)) {
      return value.intValue();
    }
    else if (Double.class.getName().equals(wrapperClassName)) {
      return value.doubleValue();
    }
    else if (Long.class.getName().equals(wrapperClassName)) {
      return value;
    }
    else if (Boolean.class.getName().equals(wrapperClassName)) {
      return value.intValue() != 0;
    }
    else if (Float.class.getName().equals(wrapperClassName)) {
      return value.floatValue();
    }
    else if (Short.class.getName().equals(wrapperClassName)) {
      return value.shortValue();
    }
    else if (Byte.class.getName().equals(wrapperClassName)) {
      return value.byteValue();
    }
    else {
      throw new RuntimeException("unknown type");
    }
  }

  public static String qualifiedNumericEncoding(boolean escaped, Object o) {
    final String quote = escaped ? "\\" + "\"" : "\"";
    
    return "{" + quote + SerializationParts.ENCODED_TYPE + quote + ":"
            + quote + (o instanceof String ? Character.class.getName() : o.getClass().getName()) + quote + ", "
            + quote + SerializationParts.OBJECT_ID + quote + ": " + quote + o.hashCode() + quote + "," +
            quote + SerializationParts.NUMERIC_VALUE + quote + ":"
            + (o instanceof Long ? quote + String.valueOf(o) + quote : String.valueOf(o)) + "}";

  }
}
