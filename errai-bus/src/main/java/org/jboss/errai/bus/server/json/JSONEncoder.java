package org.jboss.errai.bus.server.json;

import com.sun.tools.javac.tree.Tree;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class JSONEncoder {    
    boolean defer = false;

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
            return encodeMap((Map) v);
        } else if (v instanceof Object[]) {
            return encodeArray((Object[]) v);
        } else if (v instanceof Serializable) {
            return encodeObject((Serializable) v);
        } else {
            defer = true;
            return null;
        }
    }

    public String encodeObject(Serializable o) {
        if (o == null) return "null";

        Class cls = o.getClass();

        StringBuilder build = new StringBuilder("{__EncodedType:'" + cls.getName() + "',");
        Field[] fields = cls.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) != 0
                    || fields[i].isSynthetic()) {
                continue;
            }

            String k = fields[i].getName();
            Object v = MVEL.getProperty(k, o);

            build.append(k).append(":").append(_encode(v));

            if (defer) {
                throw new RuntimeException("cannot transmit type:" + v.getClass().getName());
            }

            if (i + 1 < fields.length) build.append(",");
        }

        return build.append("}").toString();
    }

    public String encodeMap(Map<Object, Object> map) {
        StringBuilder mapBuild = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            String val = _encode(entry.getValue());
            if (!defer) {
                if (!first) {
                    mapBuild.append(",");
                }
                mapBuild.append(_encode(entry.getKey()))
                        .append(":").append(val);

                first = false;
            } else {
                defer = false;
            }

        }

        return mapBuild.append("}").toString();
    }

    private String encodeCollection(Collection col) {
        StringBuilder buildCol = new StringBuilder("[");
        Iterator iter = col.iterator();
        while (iter.hasNext()) {
            buildCol.append(_encode(iter.next()));
            if (iter.hasNext()) buildCol.append(buildCol.append(","));
        }
        return buildCol.append("]").toString();
    }

    private String encodeArray(Object[] array) {
        StringBuilder buildCol = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            buildCol.append(_encode(array[i]));
            if ((i + 1) < array.length) buildCol.append(",");
        }
        return buildCol.append("]").toString();
    }


}
