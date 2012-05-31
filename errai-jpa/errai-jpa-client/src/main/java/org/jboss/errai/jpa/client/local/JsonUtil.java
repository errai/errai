package org.jboss.errai.jpa.client.local;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class JsonUtil {

  /**
   * Returns a JSONValue that represents the given value, which must be one of
   * the JPA2 basic types. JPA2 basic types are Java primitive types, wrappers
   * of the primitive types, java.lang.String, java.math.BigInteger,
   * java.math.BigDecimal, java.util.Date, java.sql.Date, java.sql.Time,
   * java.sql.Timestamp, byte[], Byte[], char[], Character[], and enums. Note
   * that JPA2 also specifies java.util.Calendar and all serializable types as
   * basic types, but these are not supported because they are not GWT-compatible.
   *
   * @param value The value to convert to JSON. Must be a JPA2 basic type.
   * @return The JSON representation of {@code value}.
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
}
