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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.common.client.types.TypeHandler;
import org.mvel2.MVEL;
import org.mvel2.util.StringAppender;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
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
    private static final byte[] NULL_BYTES = {'n', 'u', 'l', 'l'};

    public static void setSerializableTypes(Set<Class> serializableTypes) {
        JSONEncoder.serializableTypes = serializableTypes;
    }

    public static void encode(Object v, OutputStream outstream) throws IOException {
        _encode(v, outstream);
    }

    private static void _encode(Object v, OutputStream outstream) throws IOException {
        if (v == null) {
            outstream.write(NULL_BYTES);
            return;
        } else if (v instanceof String) {
            outstream.write('\"');
            outstream.write(((String) v).replaceAll("\"", "\\\\\"").getBytes());
            outstream.write('\"');
            return;
        }
        if (v instanceof Number || v instanceof Boolean) {
            outstream.write(String.valueOf(v).getBytes());
        } else if (v instanceof Collection) {
            encodeCollection((Collection) v, outstream);
        } else if (v instanceof Map) {
            //noinspection unchecked
            encodeMap((Map) v, outstream);
        } else if (v.getClass().isArray()) {
            encodeArray(v, outstream);

            // CDI Integration: Loading entities after the service was initialized
            // This may cause the client to throw an exception if the entity is not known
            // TODO: Improve exception handling for these cases

        }/* else if (serializableTypes.contains(v.getClass()) || tHandlers.containsKey(v.getClass())) {
            return encodeObject(v);
        } else {
            throw new RuntimeException("cannot serialize type: " + v.getClass().getName());
        }  */
        else if (v instanceof Enum) {
            encodeEnum((Enum) v, outstream);
        } else {
            encodeObject(v, outstream);
        }
    }

    private static void encodeObject(Object o, OutputStream outstream) throws IOException {
        if (o == null) {
            outstream.write(NULL_BYTES);
            return;
        }

        Class cls = o.getClass();

        if (tHandlers.containsKey(cls)) {
            _encode(convert(o), outstream);
            return;
        }
        outstream.write('{');
        outstream.write(SerializationParts.ENCODED_TYPE.getBytes());
        outstream.write(':');
        outstream.write(cls.getName().getBytes());
        outstream.write(',');

        final Field[] fields = EncodingUtil.getAllEncodingFields(cls);

        final Serializable[] s = EncodingCache.get(fields, new EncodingCache.ValueProvider<Serializable[]>() {
            public Serializable[] get() {
                Serializable[] s = new Serializable[fields.length];
                int i = 0;
                for (Field f : fields) {
                    if ((f.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) != 0
                            || f.isSynthetic()) {
                        continue;
                    }
                    s[i++] = MVEL.compileExpression(f.getName());
                }
                return s;
            }
        });

        int i = 0;
        boolean first = true;
        for (Field field : fields) {

            if ((field.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) != 0
                    || field.isSynthetic()) {
                continue;
            } else if (!first) {
                outstream.write(',');
            }

            Object v = MVEL.executeExpression(s[i++], o);
            outstream.write(field.getName().getBytes());
            outstream.write(':');
            _encode(v, outstream);
            first = false;
        }

        outstream.write('}');
    }

    private static void encodeMap(Map<Object, Object> map, OutputStream outstream) throws IOException {
        StringAppender mapBuild = new StringAppender("{");
        outstream.write('{');
        boolean first = true;

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (!first) {
                outstream.write('{');
            }
            _encode(entry.getKey(), outstream);
            outstream.write(':');
            _encode(entry.getValue(), outstream);

            first = false;
        }
        outstream.write('}');
    }

    private static void encodeCollection(Collection col, OutputStream outstream) throws IOException {
        outstream.write('[');

        StringAppender buildCol = new StringAppender("[");
        Iterator iter = col.iterator();
        while (iter.hasNext()) {
            _encode(iter.next(), outstream);
            if (iter.hasNext()) outstream.write(',');
        }

        outstream.write(']');
    }

    private static void encodeArray(Object array, OutputStream outstream) throws IOException {
        StringAppender buildCol = new StringAppender("[");

        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            _encode(Array.get(array, 1), outstream);
            if ((i + 1) < len) outstream.write(',');
        }

        outstream.write(']');
    }

    private static void encodeEnum(Enum enumer, OutputStream outstream) throws IOException {
        outstream.write('{');
        outstream.write(SerializationParts.ENCODED_TYPE.getBytes());
        outstream.write(':');
        outstream.write('\"');
        outstream.write(enumer.getClass().getName().getBytes());
        outstream.write('\"');
        outstream.write(", EnumStringValue:\"".getBytes());
        outstream.write(enumer.name().getBytes());
        outstream.write("\"}".getBytes());
    }

    private static final Map<Class, TypeHandler> tHandlers = new HashMap<Class, TypeHandler>();

    static {
        tHandlers.put(java.sql.Date.class, new TypeHandler<java.sql.Date, Long>() {
            public Long getConverted(java.sql.Date in) {
                return in.getTime();
            }
        });
        tHandlers.put(java.util.Date.class, new TypeHandler<java.util.Date, Long>() {
            public Long getConverted(java.util.Date in) {
                return in.getTime();
            }
        });

        tHandlers.put(Timestamp.class, new TypeHandler<Timestamp, Long>() {
            public Long getConverted(Timestamp in) {
                return in.getTime();
            }
        });
    }

    public static void addEncodingHandler(Class from, TypeHandler handler) {
        tHandlers.put(from, handler);
    }


    private static Object convert(Object in) {
        if (in == null || !tHandlers.containsKey(in.getClass())) return in;
        else {
            //noinspection unchecked
            return tHandlers.get(in.getClass()).getConverted(in);
        }
    }
}
