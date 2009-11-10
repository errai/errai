package org.jboss.errai.bus.client.types;

import com.google.gwt.json.client.*;
import org.jboss.errai.bus.client.json.JSONDecoderCli;

import java.util.*;


public class JSONTypeHelper {
    public static <T> T convert(JSONValue value, Class<? extends T> to) {

        JSONValue v;
        if ((v = value.isString()) != null) {
            return TypeHandlerFactory.convert(String.class, to, ((JSONString)v).stringValue());
        }
        else if ((v = value.isNumber()) != null) {
            return TypeHandlerFactory.convert(Number.class, to, ((JSONNumber)v).doubleValue());
        }
        else if ((v = value.isBoolean()) != null) {
            return TypeHandlerFactory.convert(Boolean.class, to, ((JSONBoolean)v).booleanValue());
        }
        else if ((v = value.isArray()) != null) {
            List list = new ArrayList();
            JSONArray arr = (JSONArray) v;

            for (int i = 0; i < arr.size(); i++) {
                list.add(convert(arr.get(i), to));
            }

            return TypeHandlerFactory.convert(Collection.class, to, list);
        }
        else if ((v = value.isObject()) != null) {
            JSONObject eMap = (JSONObject) v;

            Map<String, Object> m = new HashMap<String, Object>();

             for (String key : eMap.keySet()) {
                 if ("__EncodedType".equals(key)) {
                     String className = eMap.get(key).isString().stringValue();

                     if (TypeDemarshallers.hasDemarshaller(className)) {
                         return (T) TypeDemarshallers.getDemarshaller(className).demarshall(eMap);
                     }
                     else {
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
        }
        else if (v instanceof Character) {
            return "'" + v + "'";
        }
        else {
            return String.valueOf(v);
        }
    }
}
