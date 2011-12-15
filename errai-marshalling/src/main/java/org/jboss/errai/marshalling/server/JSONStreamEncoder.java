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

package org.jboss.errai.marshalling.server;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.marshalling.client.util.NumbersUtils;
import org.jboss.errai.marshalling.rebind.DefinitionsFactory;
import org.jboss.errai.marshalling.server.api.ServerMarshaller;
import org.jboss.errai.marshalling.server.util.ServerEncodingUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Mike Brock
 */
public class JSONStreamEncoder {
  private static final byte[] NULL_BYTES = "null".getBytes();

  public static void encode(Object v, OutputStream outstream) throws IOException {
    encode(v, outstream, new EncodingSession(MappingContextSingleton.get()));
  }

  public static void encode(Object v, OutputStream outstream, EncodingSession ctx) throws IOException {
    encode(v, outstream, ctx, false);
  }

  public static void encode(Object v, OutputStream outstream, EncodingSession ctx, boolean qualifiedNumerics)
          throws IOException {
    if (v == null) {
      outstream.write(NULL_BYTES);
      return;
    }
    else if (v instanceof String) {
      ServerEncodingUtil.write(outstream, ctx, "\"" + MarshallUtil.jsonStringEscape(v.toString()) + "\"");
      return;
    }

    if (MarshallUtil.isPrimitiveWrapper(v.getClass())) {
      if (v instanceof Character) {
        if (qualifiedNumerics) {
          ServerEncodingUtil.write(outstream, ctx, NumbersUtils.qualifiedNumericEncoding("\"" + v + "\""));
        }
        else {
          ServerEncodingUtil.write(outstream, ctx, "\"" + MarshallUtil.jsonStringEscape(v.toString()) + "\"");
        }
      }
      else if (qualifiedNumerics) {
        ServerEncodingUtil.write(outstream, ctx, NumbersUtils.qualifiedNumericEncoding(v));
      }
      else {
        outstream.write(String.valueOf(v).getBytes());
      }
    }
    else if (v instanceof Map) {
      //noinspection unchecked
      encodeMap((Map) v, outstream, ctx);
    }
    else if (v.getClass().isArray()) {
      encodeArray(v, outstream, ctx);
    }
    else if (v instanceof Enum) {
      encodeEnum((Enum) v, outstream, ctx);
    }
    else {
      encodeObject(v, outstream, ctx);
    }
  }

  private static void encodeObject(Object o, OutputStream outstream, final EncodingSession ctx) throws IOException {
    if (o == null) {
      outstream.write(NULL_BYTES);
      return;
    }

    Class cls = o.getClass();
    DefinitionsFactory defs = ctx.getMappingContext().getDefinitionsFactory();

    if (!defs.hasDefinition(cls)) {
      throw new RuntimeException("no available marshaller for: " + cls.getName());
    }

    Marshaller<Object, Object> marshaller = defs.getDefinition(cls).getMarshallerInstance();
    if (marshaller instanceof ServerMarshaller) {
      ((ServerMarshaller) marshaller).marshall(outstream, o, ctx);
    }
    else {
      ServerEncodingUtil.write(outstream, ctx, marshaller.marshall(o, ctx));
    }
  }

  private static void encodeMap(Map<Object, Object> map, OutputStream outstream, EncodingSession ctx)
          throws IOException {

    outstream.write('{');
    boolean first = true;

    for (Map.Entry<Object, Object> entry : map.entrySet()) {
      if (!first) {
        outstream.write(',');
      }

      if (!(entry.getKey() instanceof String)) {
        ServerEncodingUtil.write(outstream, ctx, '\"');
        if (!ctx.isEscapeMode()) outstream.write(SerializationParts.EMBEDDED_JSON.getBytes());
        ctx.setEscapeMode();
        encode(entry.getKey(), outstream, ctx, true);
        ctx.unsetEscapeMode();
        ServerEncodingUtil.write(outstream, ctx, '\"');
      }
      else {
        encode(entry.getKey(), outstream, ctx, true);
      }

      outstream.write(':');
      encode(entry.getValue(), outstream, ctx, true);

      first = false;
    }
    outstream.write('}');
  }

  private static void encodeCollection(Collection col, OutputStream outstream, EncodingSession ctx) throws IOException {
    outstream.write('[');
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      encode(iter.next(), outstream, ctx, true);
      if (iter.hasNext()) outstream.write(',');
    }

    outstream.write(']');
  }

  private static void encodeArray(Object array, OutputStream outstream, EncodingSession ctx) throws IOException {
    outstream.write('[');
    int len = Array.getLength(array);
    for (int i = 0; i < len; i++) {
      encode(Array.get(array, i), outstream, ctx, true);
      if ((i + 1) < len) outstream.write(',');
    }

    outstream.write(']');
  }

  private static void encodeEnum(Enum enumer, OutputStream outstream, EncodingSession ctx) throws IOException {
    ServerEncodingUtil.write(outstream, ctx, "{\"" + SerializationParts.ENCODED_TYPE + "\":\""
            + enumer.getDeclaringClass().getName() + "\""
            + ",\"" + SerializationParts.ENUM_STRING_VALUE + "\":\"" + enumer.name() + "\"}");
  }


}
