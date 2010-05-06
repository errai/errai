/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.common.client.types.Marshaller;
import org.jboss.errai.common.client.types.TypeMarshallers;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.jboss.errai.common.client.types.TypeMarshallers.getMarshaller;

public class JSONEncoderCli {
    boolean defer;

    String marshall;
    private Map<String, String> marshalledTypes;

    public String encode(Object v) {
        return _encode(v);
    }

    @SuppressWarnings({"unchecked"})
    public String _encode(Object v) {
        if (v == null) {
            return "null";
        } else if (v instanceof String) {
            return "\"" + ((String) v).replaceAll("\\\\\"", "\\\\\\\\\"").replaceAll("\"", "\\\\\"") + "\"" +
                    "";
        } else if (v instanceof Number || v instanceof Boolean) {
            return String.valueOf(v);
        } else if (v instanceof Collection) {
            return encodeCollection((Collection) v);
        } else if (v instanceof Map) {
            return encodeMap((Map<Object, Object>) v);
        } else if (v instanceof Object[]) {
            return encodeArray((Object[]) v);
        } else if (v instanceof Serializable) {
            if (TypeMarshallers.hasMarshaller(v.getClass().getName())) {
                Marshaller<Object> m = getMarshaller(marshall = v.getClass().getName());
                return m.marshall(v);
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
            mapBuild.append("," + SerializationParts.MARSHALLED_TYPES + ":\"");
            first = true;
            for (Map.Entry<String, String> m : marshalledTypes.entrySet()) {
                if (!first) {
                    mapBuild.append(',');
                }
                mapBuild.append(m.getKey());
                first = false;
            }
            mapBuild.append("\"");
        }

        return mapBuild.append("}").toString();
    }

    private String encodeCollection(Collection col) {
        StringBuilder buildCol = new StringBuilder("[");
        Iterator iter = col.iterator();
        while (iter.hasNext()) {
            buildCol.append(_encode(iter.next()));
            if (iter.hasNext()) buildCol.append(',');
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
