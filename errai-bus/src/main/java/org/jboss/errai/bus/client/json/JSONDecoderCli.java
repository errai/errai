package org.jboss.errai.bus.client.json;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.bus.client.types.Demarshaller;
import org.jboss.errai.bus.client.types.TypeDemarshallers;
import org.jboss.errai.bus.client.types.TypeHandlerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JSONDecoderCli {

    public Object decode(Object value) {
        return _decode(JSONParser.parse(String.valueOf(value)));
    }

    private Object _decode(JSONValue v) {
        if (v.isString() != null) {
            return v.isString().stringValue();
        } else if (v.isNumber() != null) {
            return v.isNumber().doubleValue();
        } else if (v.isBoolean() != null) {
            return v.isBoolean().booleanValue();
        } else if (v.isNull() != null) {
            return null;
        } else if (v instanceof JSONObject) {
            return decodeObject(v.isObject());
        } else if (v instanceof JSONArray) {
            return decodeList(v.isArray());
        } else {
            throw new RuntimeException("unknown encoding");
        }

    }

    private Object decodeObject(JSONObject eMap) {
        Map<String, Object> m = new HashMap<String, Object>();

        for (String key : eMap.keySet()) {
            if ("__EncodedType".equals(key)) {
                String className = eMap.get(key).isString().stringValue();

                System.out.println("about to bind for:" + className + ":" + key);
                if (TypeDemarshallers.hasDemarshaller(className)) {
                    return TypeDemarshallers.getDemarshaller(className).demarshall(eMap);
                }
                else {
                    GWT.log("Could not demartial class. There is no available demarshaller. " +
                            "Ensure you have exposed the class with @ExposeEntity.", null);
                    throw new RuntimeException("no available demarshaller: " + className);
                }
            }

            m.put(key, _decode(eMap.get(key)));
        }

        return m;
    }

    private List<Object> decodeList(JSONArray arr) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < arr.size(); i++) {
            list.add(_decode(arr.get(i)));
        }
        return list;
    }

}
