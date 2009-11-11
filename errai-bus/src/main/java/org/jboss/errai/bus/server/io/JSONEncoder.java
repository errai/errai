package org.jboss.errai.bus.server.io;

import org.jboss.errai.common.client.types.TypeHandler;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JSONEncoder {
    public String encode(Object v) {
        return _encode(v);
    }

    public String _encode(Object v) {
        if (v == null) {
            return "null";
        } else if (v instanceof String) {
            return "\"" + ((String) v).replaceAll("\"", "\\\\\"") + "\"";
        }
        if (v instanceof Number || v instanceof Boolean) {
            return String.valueOf(v);
        } else if (v instanceof Collection) {
            return encodeCollection((Collection) v);
        } else if (v instanceof Map) {
            //noinspection unchecked
            return encodeMap((Map) v);
        } else if (v instanceof Object[]) {
            return encodeArray((Object[]) v);
        } else if (v instanceof Serializable) {
            return encodeObject((Serializable) v);
        } else {
            return null;
        }
    }

    public String encodeObject(Serializable o) {
        if (o == null) return "null";

        Class cls = o.getClass();

        if (tHandlers.containsKey(cls)) {
            return _encode(convert(o));
        }
        
        StringBuilder build = new StringBuilder("{__EncodedType:'" + cls.getName() + "',");
        Field[] fields = cls.getDeclaredFields();

        String k;
        boolean first = true;
        for (Field field : fields) {
            if ((field.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) != 0
                    || field.isSynthetic()) {
                continue;
            } else if (!first) {
                build.append(',');
            }

            build.append(k = field.getName()).append(':').append(_encode(MVEL.getProperty(k, o)));
            first = false;
        }

        return build.append('}').toString();
    }

    public String encodeMap(Map<Object, Object> map) {
        StringBuilder mapBuild = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            String val = _encode(entry.getValue());
            if (!first) {
                mapBuild.append(',');
            }
            mapBuild.append(_encode(entry.getKey()))
                    .append(':').append(val);

            first = false;
        }

        return mapBuild.append('}').toString();
    }

    private String encodeCollection(Collection col) {
        StringBuilder buildCol = new StringBuilder("[");
        Iterator iter = col.iterator();
        while (iter.hasNext()) {
            buildCol.append(_encode(iter.next()));
            if (iter.hasNext()) buildCol.append(',');
        }
        return buildCol.append(']').toString();
    }

    private String encodeArray(Object[] array) {
        StringBuilder buildCol = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            buildCol.append(_encode(array[i]));
            if ((i + 1) < array.length) buildCol.append(',');
        }
        return buildCol.append(']').toString();
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
    }

    private static Object convert(Object in) {
        if (in == null || !tHandlers.containsKey(in.getClass())) return in;
        else {
            //noinspection unchecked
            return tHandlers.get(in.getClass()).getConverted(in);
        }
    }  
}
