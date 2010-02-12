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

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.errai.common.client.types.TypeDemarshallers.getDemarshaller;
import static org.jboss.errai.common.client.types.TypeDemarshallers.hasDemarshaller;


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
                if (hasDemarshaller(className)) {
                    return getDemarshaller(className).demarshall(eMap);
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
