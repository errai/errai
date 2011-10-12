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

package org.jboss.errai.common.client.json;

import org.jboss.errai.common.client.types.DataTypeHelper;
import org.jboss.errai.common.client.types.EncodingContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class JSONEncoderCli {
  boolean defer;

  String marshall;
  private Map<String, String> marshalledTypes;

  public String encode(Object v, EncodingContext ctx) {
    return _encode(v, ctx);
  }

  @SuppressWarnings({"unchecked"})
  public String _encode(Object v, EncodingContext ctx) {
    if (v == null) {
      return "null";
    }
    else if (v instanceof String) {
      return encodeString((String) v, ctx);
    }
    else if (v instanceof Number || v instanceof Boolean) {
      return String.valueOf(v);
    }
    else if (v instanceof Collection) {
      return encodeCollection((Collection) v, ctx);
    }
    else if (v instanceof Map) {
      return encodeMap((Map<Object, Object>) v, ctx);
    }
    else if (v instanceof Object[]) {
      return encodeArray((Object[]) v, ctx);
    }
    else if (v.getClass().isArray()) {
      if (v instanceof char[]) {
        return encodeArray((char[]) v, ctx);
      }
      else if (v instanceof int[]) {
        return encodeArray((int[]) v, ctx);
      }
      else if (v instanceof double[]) {
        return encodeArray((double[]) v, ctx);
      }
      else if (v instanceof long[]) {
        return encodeArray((long[]) v, ctx);
      }
      else if (v instanceof boolean[]) {
        return encodeArray((boolean[]) v, ctx);
      }
      else if (v instanceof byte[]) {
        return encodeArray((byte[]) v, ctx);
      }
      else if (v instanceof short[]) {
        return encodeArray((short[]) v, ctx);
      }
      else if (v instanceof float[]) {
        return encodeArray((float[]) v, ctx);
      }
      return null;
    }
    else if (DataTypeHelper.getMarshallerProvider().hasMarshaller(v.getClass().getName())) {
//      Marshaller<Object> m = getMarshaller(marshall = v.getClass().getName());
//      String enc = m.marshall(v, ctx);
      return DataTypeHelper.getMarshallerProvider().marshall(v.getClass().getName(), v);
    }
    else if (v instanceof Enum) {
      return _encode(v.toString(), ctx);
    }
    else {
      defer = true;
      return null;
    }
  }

  public static String encodeString(String string, EncodingContext ctx) {
    return "\"" + string.replaceAll("\\\\", "\\\\\\\\").replaceAll("[\\\\]{0}\\\"", "\\\\\"") + "\"";
  }


  public String encodeMap(Map<Object, Object> map, EncodingContext ctx) {
    StringBuilder mapBuild = new StringBuilder("{");
    boolean first = true;

    for (Map.Entry<Object, Object> entry : map.entrySet()) {
      String val = _encode(entry.getValue(), ctx);
      if (!defer) {
        if (!first) {
          mapBuild.append(",");
        }
        mapBuild.append(_encode(entry.getKey(), ctx))
                .append(":").append(val);


        first = false;
      }
      else {
        defer = false;
      }
    }

    return mapBuild.append("}").toString();
  }

  private String encodeCollection(Collection col, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      buildCol.append(_encode(iter.next(), ctx));
      if (iter.hasNext()) buildCol.append(',');
    }
    return buildCol.append("]").toString();
  }

  private String encodeArray(Object[] array, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      buildCol.append(_encode(array[i], ctx));
      if ((i + 1) < array.length) buildCol.append(",");
    }
    return buildCol.append("]").toString();
  }

  private String encodeArray(char[] array, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      buildCol.append(_encode(array[i], ctx));
      if ((i + 1) < array.length) buildCol.append(",");
    }
    return buildCol.append("]").toString();
  }

  private String encodeArray(int[] array, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      buildCol.append(_encode(array[i], ctx));
      if ((i + 1) < array.length) buildCol.append(",");
    }
    return buildCol.append("]").toString();
  }

  private String encodeArray(long[] array, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      buildCol.append(_encode(array[i], ctx));
      if ((i + 1) < array.length) buildCol.append(",");
    }
    return buildCol.append("]").toString();
  }

  private String encodeArray(short[] array, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      buildCol.append(_encode(array[i], ctx));
      if ((i + 1) < array.length) buildCol.append(",");
    }
    return buildCol.append("]").toString();
  }

  private String encodeArray(double[] array, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      buildCol.append(_encode(array[i], ctx));
      if ((i + 1) < array.length) buildCol.append(",");
    }
    return buildCol.append("]").toString();
  }

  private String encodeArray(float[] array, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      buildCol.append(_encode(array[i], ctx));
      if ((i + 1) < array.length) buildCol.append(",");
    }
    return buildCol.append("]").toString();
  }

  private String encodeArray(boolean[] array, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      buildCol.append(_encode(array[i], ctx));
      if ((i + 1) < array.length) buildCol.append(",");
    }
    return buildCol.append("]").toString();
  }

  private String encodeArray(byte[] array, EncodingContext ctx) {
    StringBuilder buildCol = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      buildCol.append(_encode(array[i], ctx));
      if ((i + 1) < array.length) buildCol.append(",");
    }
    return buildCol.append("]").toString();
  }


  public Map<String, String> getMarshalledTypes() {
    return marshalledTypes;
  }
}
