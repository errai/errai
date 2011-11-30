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
import org.jboss.errai.common.client.types.DecodingContext;
import org.jboss.errai.common.client.types.EncodingContext;
import org.jboss.errai.common.client.types.TypeHandler;
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
import java.sql.Timestamp;
import java.util.*;

/**
 * User: christopherbrock
 * Date: 21-Jul-2010
 * Time: 10:30:12 PM
 */
public class JSONStreamEncoder {
  private static Set<Class> serializableTypes;
  private static final byte[] NULL_BYTES = "null".getBytes();


  public static void setSerializableTypes(Set<Class> serializableTypes) {
    JSONEncoder.SERIALIZABLE_TYPES = serializableTypes;
  }

  public static void encode(Object v, OutputStream outstream) throws IOException {
    _encode(v, outstream, new EncodingContext());
  }

  private static void _encode(Object v, OutputStream outstream, EncodingContext ctx) throws IOException {
    _encode(v, outstream, ctx, false);
  }

  private static void _encode(Object v, OutputStream outstream, EncodingContext ctx, boolean qualifiedNumerics) throws IOException {
    if (v == null) {
      outstream.write(NULL_BYTES);
      return;
    }
    else if (v instanceof String) {
      outstream.write('\"');
      outstream.write(((String) v).replaceAll("\"", "\\\\\"").getBytes());
      outstream.write('\"');
      return;
    }
    if (v instanceof Number || v instanceof Boolean || v instanceof Character) {
      if (qualifiedNumerics) {
        if (v instanceof Character) {
          v = "\"" + v.toString() + "\"";
        }
        outstream.write(NumbersUtils.qualifiedNumericEncoding(ctx.isEscapeMode(), v).getBytes());
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

  private static void encodeObject(Object o, OutputStream outstream, EncodingContext ctx) throws IOException {
    if (o == null) {
      outstream.write(NULL_BYTES);
      return;
    }

    Class cls = o.getClass();


    if (java.util.Date.class.isAssignableFrom(cls)) {
      outstream.write(("{\"__EncodedType\":\"java.util.Date\", \"__ObjectID\":\"" + o.hashCode() + "\", \"Value\":" + ((java.util.Date) o).getTime() + "}").getBytes());
      return;
    }
    if (java.sql.Date.class.isAssignableFrom(cls)) {
      outstream.write(("{\"__EncodedType\":\"java.sql.Date\", \"__ObjectID\":\"" + o.hashCode() + "\", \"Value\":" + ((java.sql.Date) o).getTime() + "}").getBytes());
      return;
    }

    if (tHandlers.containsKey(cls)) {
      _encode(convert(o), outstream, ctx);
      return;
    }


    if (ctx.isEncoded(o)) {
      /**
       * If this object is referencing a duplicate object in the graph, we only provide an ID reference.
       */
      write(outstream, ctx, "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + cls.getCanonicalName() + "\",\"" + SerializationParts.OBJECT_ID + "\":\"" + ctx.markRef(o) + "\"}");

      return;
    }

    outstream.write('{');
    outstream.write('\"');
    outstream.write(SerializationParts.ENCODED_TYPE.getBytes());
    outstream.write('\"');
    outstream.write(':');
    outstream.write('\"');
    outstream.write(cls.getCanonicalName().getBytes());
    outstream.write('\"');
    outstream.write(',');
    outstream.write('\"');
    outstream.write(SerializationParts.OBJECT_ID.getBytes());
    outstream.write('\"');
    outstream.write(':');
    outstream.write('\"');
    outstream.write(String.valueOf(ctx.markRef(o)).getBytes());
    outstream.write('\"');
    outstream.write(',');

    int i = 0;
    boolean first = true;

    if (DefinitionsFactory.hasDefinition(cls)) {
      MappingDefinition def = DefinitionsFactory.getDefinition(cls);

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

        outstream.write('\"');
        outstream.write(mapping.getKey().getBytes());
        outstream.write('\"');
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
          outstream.write('\"');
          outstream.write(field.getName().getBytes());
          outstream.write('\"');
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

  private static void encodeMap(Map<Object, Object> map, OutputStream outstream, EncodingContext ctx) throws IOException {
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

  private static void encodeCollection(Collection col, OutputStream outstream, EncodingContext ctx) throws IOException {
    outstream.write('[');

    //  StringAppender buildCol = new StringAppender("[");
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      _encode(iter.next(), outstream, ctx, true);
      if (iter.hasNext()) outstream.write(',');
    }

    outstream.write(']');
  }

  private static void encodeArray(Object array, OutputStream outstream, EncodingContext ctx) throws IOException {
    // StringAppender buildCol = new StringAppender("[");

    int len = Array.getLength(array);
    for (int i = 0; i < len; i++) {
      _encode(Array.get(array, 1), outstream, ctx);
      if ((i + 1) < len) outstream.write(',');
    }

    outstream.write(']');
  }

  private static void encodeEnum(Enum enumer, OutputStream outstream, EncodingContext ctx) throws IOException {
    outstream.write('{');
    outstream.write('\"');
    outstream.write(SerializationParts.ENCODED_TYPE.getBytes());
    outstream.write('\"');
    outstream.write(':');
    outstream.write('\"');
    outstream.write(enumer.getClass().getName().getBytes());
    outstream.write('\"');
    outstream.write(",\"EnumStringValue\":\"".getBytes());
    outstream.write(enumer.name().getBytes());
    outstream.write("\"}".getBytes());
  }

  private static final Map<Class, TypeHandler> tHandlers = new HashMap<Class, TypeHandler>();

  static {
    tHandlers.put(java.sql.Date.class, new TypeHandler<java.sql.Date, Long>() {
      public Long getConverted(java.sql.Date in, DecodingContext ctx) {
        return in.getTime();
      }
    });
    tHandlers.put(java.util.Date.class, new TypeHandler<java.util.Date, Long>() {
      public Long getConverted(java.util.Date in, DecodingContext ctx) {
        return in.getTime();
      }
    });

    tHandlers.put(Timestamp.class, new TypeHandler<Timestamp, Long>() {
      public Long getConverted(Timestamp in, DecodingContext ctx) {
        return in.getTime();
      }
    });
  }

  public static void addEncodingHandler(Class from, TypeHandler handler) {
    tHandlers.put(from, handler);
  }

  private static void write(OutputStream stream, EncodingContext ctx, String s) throws IOException {
    if (ctx.isEscapeMode()) {
      stream.write(s.replaceAll("\"", "\\\\\"").getBytes());
    }
    else {
      stream.write(s.getBytes());
    }
  }

  private static void write(OutputStream stream, EncodingContext ctx, char s) throws IOException {
    if (ctx.isEscapeMode() && s == '\"') {
      stream.write("\\\\\"".getBytes());

    }
    else {
      stream.write(s);
    }
  }

  private static final DecodingContext STATIC_DEC_CONTEXT = new DecodingContext();

  private static Object convert(Object in) {
    if (in == null || !tHandlers.containsKey(in.getClass())) return in;
    else {
      //noinspection unchecked
      return tHandlers.get(in.getClass()).getConverted(in, STATIC_DEC_CONTEXT);
    }
  }
}
