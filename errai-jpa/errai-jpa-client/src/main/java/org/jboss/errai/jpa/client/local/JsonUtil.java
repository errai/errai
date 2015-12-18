/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.client.local;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import org.jboss.errai.common.client.util.Base64Util;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * JSON-related operations required by Errai JPA.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class JsonUtil {

  /**
   * Returns a JSONValue that represents the given value, which must be one of
   * the JPA2 basic types. JPA2 basic types are Java primitive types, wrappers
   * of the primitive types, java.lang.String, java.math.BigInteger,
   * java.math.BigDecimal, java.util.Date, java.sql.Date, java.sql.Time,
   * java.sql.Timestamp, byte[], Byte[], char[], Character[], and enums. Note
   * that JPA2 also specifies java.util.Calendar and all serializable types as
   * basic types, but these are not supported because they are not
   * GWT-compatible.
   *
   * @param value
   *          The value to convert to JSON. Must be a JPA2 basic type. Null is
   *          permitted.
   * @return The JSON representation of {@code value}. Never null (but might be
   *         an instance of JSONNull).
   */
  public static JSONValue basicValueToJson(Object value) {
    if (value == null) {
      return JSONNull.getInstance();
    }
    else if (value instanceof String
            || value instanceof BigInteger || value instanceof BigDecimal

            // Long doesn't fit in a JSONNumber
            || value instanceof Long

            // Character isn't a java.lang.Number
            || value instanceof Character

            // Timestamp includes nanoseconds, and has a special String representation that is parseable
            || value instanceof Timestamp) {
      return new JSONString(value.toString());
    }
    else if (value instanceof Boolean) {
      return JSONBoolean.getInstance((Boolean) value);
    }
    else if (value instanceof Number) {
      return new JSONNumber(((Number) value).doubleValue());
    }
    else if (value instanceof Enum) {
      return new JSONString(((Enum<?>) value).name());
    }
    else if (value instanceof Date) {  // covers java.sql.[Date,Time,Timestamp]
      return new JSONString(String.valueOf(((Date) value).getTime()));
    }
    else if (value instanceof byte[]) {
      byte[] v = (byte[]) value;
      return new JSONString(Base64Util.encode(v, 0, v.length));
    }
    else if (value instanceof Byte[]) {
      Byte[] v = (Byte[]) value;
      return new JSONString(Base64Util.encode(v, 0, v.length));
    }
    else if (value instanceof char[]) {
      return new JSONString(String.copyValueOf(((char[]) value)));
    }
    else if (value instanceof Character[]) {
      Character[] v = (Character[]) value;
      StringBuilder sb = new StringBuilder(v.length);
      for (Character c : v) {
        sb.append((char) c);
      }
      return new JSONString(sb.toString());
    }
    else {
      throw new RuntimeException("I don't know how JSONify " + value + " (" + value.getClass() + ")");
    }
  }

  /**
   * Converts the given JSONValue to the Java value of the given type. No type
   * coercion is performed; the given JSONValue and Class must correspond with
   * what would have been produced by {@link #basicValueToJson(Object)}.
   *
   * @param jsonValue
   *          The value to convert from JSON. Not null.
   * @param expectedType
   *          The Java type to convert to. Not null.
   * @return The JSON representation of {@code value}. Will be null if the given
   *         JSONValue is a JSONNull.
   */
  @SuppressWarnings("unchecked")
  public static <Y> Y basicValueFromJson(JSONValue jsonValue, Class<Y> expectedType) {
    Y value;
    if (jsonValue == null || jsonValue.isNull() != null) {
      value = null;
    }
    else if (expectedType == String.class) {
      value = (Y) jsonValue.isString().stringValue();
    }
    else if (expectedType == boolean.class || expectedType == Boolean.class) {
      value = (Y) Boolean.valueOf(jsonValue.isBoolean().booleanValue());
    }
    else if (expectedType == BigInteger.class) {
      value = (Y) new BigInteger(jsonValue.isString().stringValue());
    }
    else if (expectedType == BigDecimal.class) {
      value = (Y) new BigDecimal(jsonValue.isString().stringValue());
    }
    else if (expectedType == byte.class || expectedType == Byte.class) {
      value = (Y) Byte.valueOf((byte) jsonValue.isNumber().doubleValue());
    }
    else if (expectedType == char.class || expectedType == Character.class) {
      value = (Y) Character.valueOf(jsonValue.isString().stringValue().charAt(0));
    }
    else if (expectedType == short.class || expectedType == Short.class) {
      value = (Y) Short.valueOf((short) jsonValue.isNumber().doubleValue());
    }
    else if (expectedType == int.class || expectedType == Integer.class) {
      value = (Y) Integer.valueOf((int) jsonValue.isNumber().doubleValue());
    }
    else if (expectedType == long.class || expectedType == Long.class) {
      value = (Y) Long.valueOf(jsonValue.isString().stringValue());
    }
    else if (expectedType == float.class || expectedType == Float.class) {
      value = (Y) Float.valueOf((float) jsonValue.isNumber().doubleValue());
    }
    else if (expectedType == double.class || expectedType == Double.class) {
      value = (Y) Double.valueOf(jsonValue.isNumber().doubleValue());
    }
    else if (expectedType == Date.class) {
      value = (Y) new Date(Long.parseLong(jsonValue.isString().stringValue()));
    }
    else if (expectedType == java.sql.Date.class) {
      value = (Y) new java.sql.Date(Long.parseLong(jsonValue.isString().stringValue()));
    }
    else if (expectedType == Time.class) {
      value = (Y) new Time(Long.parseLong(jsonValue.isString().stringValue()));
    }
    else if (expectedType == Timestamp.class) {
      value = (Y) Timestamp.valueOf(jsonValue.isString().stringValue());
    }
    else if (expectedType.isEnum()) {
      @SuppressWarnings("rawtypes") Class enumType = expectedType;
      value = (Y) Enum.valueOf(enumType, jsonValue.isString().stringValue());
    }
    else if (expectedType == byte[].class) {
      value = (Y) Base64Util.decode(jsonValue.isString().stringValue());
    }
    else if (expectedType == Byte[].class) {
      value = (Y) Base64Util.decodeAsBoxed(jsonValue.isString().stringValue());
    }
    else if (expectedType == char[].class) {
      value = (Y) jsonValue.isString().stringValue().toCharArray();
    }
    else if (expectedType == Character[].class) {
      String str = jsonValue.isString().stringValue();
      Character[] boxedArray = new Character[str.length()];
      for (int i = 0; i < str.length(); i++) {
        boxedArray[i] = str.charAt(i);
      }
      value = (Y) boxedArray;
    }
    else {
      throw new RuntimeException("I don't know how unJSONify " + jsonValue + " (expected type "+ expectedType + ")");
    }
    return value;
  }

  /**
   * Compares two JSON values for equality by value.
   * <p>
   * If the actual type of v1 and v2 differs, the values are considered unequal
   * (that is, type coercion is never performed when doing a comparison).
   * <p>
   * This method returns true under the following conditions:
   * <ul>
   *  <li>If v1 and v2 are both JSONString, and their String values compare equal;
   *  <li>If v1 and v2 are both JSONBoolean, and they compare equal;
   *  <li>If v1 and v2 are both JSONNumber, and their double values compare equal;
   *  <li>If v1 and v2 are both JSONNull;
   *  <li>If v1 and v2 are both JSONArray, they have the same length, and recursive
   *      application of this check to each pair of corresponding elements finds them all equal;
   *  <li>If v1 and v2 are both JSONObject, they have the same key sets as each other,
   *      and recursive application of this check to each pair of corresponding
   *      values finds them all equal;
   * </ul>
   *
   * @param v1
   *          One of the values to compare. Must not be null (but JSONNull is
   *          permitted).
   * @param v2
   *          The other value to compare. Must not be null (but JSONNull is
   *          permitted).
   * @return true if v1 and v2 have identical values according to the above criteria.
   */
  public static boolean equals(JSONValue v1, JSONValue v2) {
    if (v1.equals(v2)) {
      return true;
    }
    else if (v1.getClass() != v2.getClass()) {
      return false;
    }
    else if (v1.isArray() != null) {
      JSONArray a1 = v1.isArray();
      JSONArray a2 = v2.isArray();
      if (a1.size() != a2.size()) {
        return false;
      }
      for (int i = 0, n = a1.size(); i < n; i++) {
        if (!equals(a1.get(i), a2.get(i))) {
          return false;
        }
      }
      return true;
    }
    else if (v1.isBoolean() != null) {
      JSONBoolean b1 = v1.isBoolean();
      JSONBoolean b2 = v2.isBoolean();
      return b1.booleanValue() == b2.booleanValue();
    }
    else if (v1.isNull() != null) {
      // this case should never be triggered, because of the getClass() precheck above
      return v2.isNull() != null;
    }
    else if (v1.isNumber() != null) {
      JSONNumber n1 = v1.isNumber();
      JSONNumber n2 = v2.isNumber();
      return n1.doubleValue() == n2.doubleValue();
    }
    else if (v1.isObject() != null) {
      JSONObject o1 = v1.isObject();
      JSONObject o2 = v2.isObject();
      if (!o1.keySet().equals(o2.keySet())) {
        return false;
      }
      for (String key : o1.keySet()) {
        if (!equals(o1.get(key), o2.get(key))) {
          return false;
        }
      }
      return true;
    }
    else if (v1.isString() != null) {
      JSONString s1 = v1.isString();
      JSONString s2 = v2.isString();
      return s1.stringValue().equals(s2.stringValue());
    }
    else {
      throw new AssertionError("Found unexpected subtype of JSONValue: " + v1.getClass());
    }
    // NOTREACHED
  }
}
