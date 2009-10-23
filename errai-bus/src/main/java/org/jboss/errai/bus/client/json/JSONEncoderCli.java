package org.jboss.errai.bus.client.json;

import org.jboss.errai.bus.client.types.TypeMarshallers;

import java.io.Serializable;
import java.util.*;

public class JSONEncoderCli {
    boolean defer;

    String marshall;
    private Map<String, String> marshalledTypes;

    public String encode(Object v) {
        return _encode(v);
    }

    public String _encode(Object v) {
        if (v == null) {
            return "null";
        } else if (v instanceof String) {
            return "\"" + v + "\"";
        } else if (v instanceof Number || v instanceof Boolean) {
            return String.valueOf(v);
        } else if (v instanceof Collection) {
            return encodeCollection((Collection) v);
        } else if (v instanceof Map) {
            return encodeMap((Map) v);
        } else if (v instanceof Object[]) {
            return encodeArray((Object[]) v);
        } else if (v instanceof Serializable) {
            if (TypeMarshallers.hasMarshaller(v.getClass().getName())) {
                return TypeMarshallers.getMarshaller(marshall = v.getClass().getName()).marshall(v);
            } else {
                throw new RuntimeException("Unable to marshal: no available marshaller: " + v.getClass().getName());
            }
        } else {
            defer = true;
            return null;
        }
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

                if (marshall != null) {
                    if (marshalledTypes == null) marshalledTypes = new HashMap<String, String>();
                    marshalledTypes.put((String) entry.getKey(), marshall);
                    marshall = null;
                }

                first = false;
            } else {
                defer = false;
            }
        }

        if (marshalledTypes != null) {
            mapBuild.append(",__MarshalledTypes:\"");
            first = true;
            for (Map.Entry<String, String> m : marshalledTypes.entrySet()) {
                if (!first) {
                    mapBuild.append(',');
                }
                mapBuild.append(m.getKey()).append("|").append(m.getValue());
                first = false;
            }
            mapBuild.append("\"");

            System.out.println("##" + mapBuild.toString() + "}");
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

    public Map<String, String> getMarshalledTypes() {
        return marshalledTypes;
    }
}
