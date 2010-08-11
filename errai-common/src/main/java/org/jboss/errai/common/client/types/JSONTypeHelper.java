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

package org.jboss.errai.common.client.types;

import com.google.gwt.json.client.*;
import org.jboss.errai.common.client.json.JSONDecoderCli;
import org.jboss.errai.common.client.protocols.SerializationParts;

import java.util.*;


public class JSONTypeHelper {
    public static <T> T convert(JSONValue value, Class<T> to) {
        JSONValue v;
        if ((v = value.isString()) != null) {
            return TypeHandlerFactory.convert(String.class, to, ((JSONString) v).stringValue());
        } else if ((v = value.isNumber()) != null) {
            return TypeHandlerFactory.convert(Number.class, to, ((JSONNumber) v).doubleValue());
        } else if ((v = value.isBoolean()) != null) {
            return TypeHandlerFactory.convert(Boolean.class, to, ((JSONBoolean) v).booleanValue());
        } else if ((v = value.isArray()) != null) {
            List list = new ArrayList();
            JSONArray arr = (JSONArray) v;

            Class cType = to.getComponentType();

            while (cType != null && cType.getComponentType() != null)
                cType = cType.getComponentType();

            if (cType == null) cType = to;

            for (int i = 0; i < arr.size(); i++) {
                list.add(convert(arr.get(i), cType));
            }

            T t =  TypeHandlerFactory.convert(Collection.class, to, list);

            return t;
        } else if ((v = value.isObject()) != null) {
            JSONObject eMap = (JSONObject) v;

            Map<String, Object> m = new HashMap<String, Object>();

            for (String key : eMap.keySet()) {
                if (SerializationParts.ENCODED_TYPE.equals(key)) {
                    String className = eMap.get(key).isString().stringValue();

                    if (TypeDemarshallers.hasDemarshaller(className)) {
                        return (T) TypeDemarshallers.getDemarshaller(className).demarshall(eMap);
                    } else {
                        throw new RuntimeException("no available demarshaller: " + className);
                    }

                }

                m.put(key, new JSONDecoderCli().decode(eMap.get(key)));
            }

            return TypeHandlerFactory.convert(Map.class, to, m);
        }

        return null;
    }

    public static String encodeHelper(Object v) {
        if (v instanceof String) {
            return "\\\"" + v + "\\\"";
        } else if (v instanceof Character) {
            return "'" + v + "'";
        } else {
            return String.valueOf(v);
        }
    }
}
