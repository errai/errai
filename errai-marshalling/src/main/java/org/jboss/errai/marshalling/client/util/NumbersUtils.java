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

  public static String qualifiedNumericEncoding(Object o) {
    return "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + o.getClass().getName() + "\", \"" +
            SerializationParts.OBJECT_ID + "\": \"" + o.hashCode() + "\"," +
            "\"" + SerializationParts.NUMERIC_VALUE + "\":"
            + (o instanceof Long ? "\"" + String.valueOf(o) + "\"" : String.valueOf(o)) + "}";

  }
}
