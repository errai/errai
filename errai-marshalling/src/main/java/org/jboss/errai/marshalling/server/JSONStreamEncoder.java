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

import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.marshalling.client.util.NumbersUtils;
import org.jboss.errai.marshalling.rebind.DefinitionsFactory;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Mike Brock
 */
public class JSONStreamEncoder {
  private static final byte[] NULL_BYTES = "null".getBytes();

  public static void encode(Object v, OutputStream outstream) throws IOException {
    _encode(v, outstream, new EncodingSession(MappingContextSingleton.get()));
  }

  private static void _encode(Object v, OutputStream outstream, EncodingSession ctx) throws IOException {
    _encode(v, outstream, ctx, false);
  }

  private static void _encode(Object v, OutputStream outstream, EncodingSession ctx, boolean qualifiedNumerics) throws IOException {
    if (v == null) {
      outstream.write(NULL_BYTES);
      return;
    }
    else if (v instanceof String) {
      write(outstream, ctx, "\"" + MarshallUtil.jsonStringEscape(v.toString()) + "\"");
      return;
    }
    if (v instanceof Number || v instanceof Boolean || v instanceof Character) {
      if (v instanceof Character) {
        if (qualifiedNumerics) {
          write(outstream, ctx, NumbersUtils.qualifiedNumericEncoding(false, "\"" + v + "\""));
        }
        else {
          write(outstream, ctx, "\"" + MarshallUtil.jsonStringEscape(v.toString()) + "\"");
        }
      }
      else if (qualifiedNumerics) {
        write(outstream, ctx, NumbersUtils.qualifiedNumericEncoding(ctx.isEscapeMode(), v));
      }
      else {
        outstream.write(String.valueOf(v).getBytes());
      }
    }
    else if (v instanceof Collection) {
      encodeCollection((Collection) v, outstream, ctx);
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
    boolean enc = ctx.isEncoded(o);
    String hash = ctx.getObjectHash(o);

    if (ctx.hasMarshaller(cls.getName())) {
      write(outstream, ctx, ctx.getMarshallerInstance(cls.getName()).marshall(o, ctx));
      return;
    }

    if (enc) {
      /**
       * If this object is referencing a duplicate object in the graph, we only provide an ID reference.
       */
      write(outstream, ctx, "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + cls.getCanonicalName() + "\",\"" + SerializationParts.OBJECT_ID + "\":\"" + hash + "\"}");

      return;
    }
    else {
      write(outstream, ctx, "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + cls.getCanonicalName() + "\",\"" + SerializationParts.OBJECT_ID + "\":\"" + hash + "\",");
    }

    int i = 0;
    boolean first = true;

    DefinitionsFactory defs = ctx.getMappingContext().getDefinitionsFactory();
    if (defs.hasDefinition(cls)) {
      MappingDefinition def = defs.getDefinition(cls);

      for (MemberMapping mapping : def.getReadableMemberMappings()) {
        if (!first) {
          outstream.write(',');
        }

        i++;
        Object v;

        if (mapping.getReadingMember() instanceof MetaField) {
          Field field = ((MetaField) mapping.getReadingMember()).asField();

          try {
            v = field.get(o);
          }
          catch (Exception e) {
            throw new RuntimeException("error accessing field: " + field, e);
          }
        }
        else {
          Method method = ((MetaMethod) mapping.getReadingMember()).asMethod();

          try {
            v = method.invoke(o);
          }
          catch (Exception e) {
            throw new RuntimeException("error calling getter: " + method, e);
          }
        }

        write(outstream, ctx, "\"" + mapping.getKey() + "\"");
        outstream.write(':');
        _encode(v, outstream, ctx);

        first = false;
      }
    }
    else {
      final Field[] fields = EncodingUtil.getAllEncodingFields(cls);

      for (Field field : fields) {
        if ((field.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) != 0
                || field.isSynthetic()) {
          continue;
        }
        else if (!first) {
          outstream.write(',');
        }

        try {
          i++;
          Object v = field.get(o);
          write(outstream, ctx, "\"" + field.getName() + "\"");
          outstream.write(':');
          _encode(v, outstream, ctx);
          first = false;
        }
        catch (Exception e) {
          throw new RuntimeException("error serializing field: " + field, e);
        }
      }
    }

    if (i == 0) {
      write(outstream, ctx, "\"" + SerializationParts.INSTANTIATE_ONLY + "\":true");
    }

    outstream.write('}');

  }

  private static void encodeMap(Map<Object, Object> map, OutputStream outstream, EncodingSession ctx) throws IOException {
    //  StringAppender mapBuild = new StringAppender("{");
    outstream.write('{');
    boolean first = true;

    for (Map.Entry<Object, Object> entry : map.entrySet()) {
      if (!first) {
        outstream.write(',');
      }

      if (!(entry.getKey() instanceof String)) {
        write(outstream, ctx, '\"');
        if (!ctx.isEscapeMode()) outstream.write(SerializationParts.EMBEDDED_JSON.getBytes());
        ctx.setEscapeMode();
        _encode(entry.getKey(), outstream, ctx, true);
        ctx.unsetEscapeMode();
        write(outstream, ctx, '\"');
      }
      else {
        _encode(entry.getKey(), outstream, ctx, true);
      }

      outstream.write(':');
      _encode(entry.getValue(), outstream, ctx);

      first = false;
    }
    outstream.write('}');
  }

  private static void encodeCollection(Collection col, OutputStream outstream, EncodingSession ctx) throws IOException {
    outstream.write('[');
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      _encode(iter.next(), outstream, ctx, true);
      if (iter.hasNext()) outstream.write(',');
    }

    outstream.write(']');
  }

  private static void encodeArray(Object array, OutputStream outstream, EncodingSession ctx) throws IOException {
    // StringAppender buildCol = new StringAppender("[");

    outstream.write('[');
    int len = Array.getLength(array);
    for (int i = 0; i < len; i++) {
      _encode(Array.get(array, i), outstream, ctx, true);
      if ((i + 1) < len) outstream.write(',');
    }

    outstream.write(']');
  }

  private static void encodeEnum(Enum enumer, OutputStream outstream, EncodingSession ctx) throws IOException {
    write(outstream, ctx, "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + enumer.getClass().getName() + "\""
            + ",\"EnumStringValue\":\"" + enumer.name() + "\"}");
  }


  private static void write(OutputStream stream, EncodingSession ctx, String s) throws IOException {
    if (ctx.isEscapeMode()) {
      stream.write(s.replaceAll("\"", "\\\\\"").getBytes());
    }
    else {
      stream.write(s.getBytes());
    }
  }

  private static void write(OutputStream stream, EncodingSession ctx, char s) throws IOException {
    if (ctx.isEscapeMode() && s == '\"') {
      stream.write("\\\\\"".getBytes());

    }
    else {
      stream.write(s);
    }
  }
}
