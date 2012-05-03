/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.enterprise.client.jaxrs;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * Utility to transform Errai's JSON to a Jackson compatible JSON and vice versa.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JacksonTransformer {
  private static final String ENCODED_TYPE = "^EncodedType";
  private static final String VALUE = "^Value";
  private static final String OBJECT_ID = "^ObjectID";
  private static final String ENUM_STRING_VALUE = "^EnumStringValue";

  private JacksonTransformer() {};

  public static String toJackson(String erraiJson) {
    JSONValue val = JSONParser.parseStrict(erraiJson);
    if (val.isObject() != null) {
      toJackson(val, null, null, new HashMap<String, JSONValue>());
    }

    return val.toString();
  }

  private static void toJackson(JSONValue val, String key, JSONObject parent, Map<String, JSONValue> backReferences) {
    JSONObject obj;
    JSONArray arr;
    if ((obj = val.isObject()) != null) {
      JSONValue objectId = obj.get(OBJECT_ID);
      if (objectId != null) {
        JSONValue backRef = backReferences.get(objectId.toString());
        if (backRef != null) {
          parent.put(key, backRef);
        }
        else {
          backReferences.put(obj.get(OBJECT_ID).toString(), obj);
        }
      }

      obj.put(OBJECT_ID, null);
      obj.put(ENCODED_TYPE, null);

      for (String k : obj.keySet()) {
        if ((arr = obj.get(k).isArray()) != null) {
          for (int i = 0; i < arr.size(); i++) {
            toJackson(arr.get(i), VALUE, obj, backReferences);
          }
          parent.put(key, obj.get(k));
        }
        else if (k.equals(ENUM_STRING_VALUE)) {
          parent.put(key, obj.get(k));
        }

        toJackson(obj.get(k), k, obj, backReferences);
      }
    }
  }

  public static String fromJackson(String jackson) {
    JSONValue val = JSONParser.parseStrict(jackson);
    if (val.isObject() != null) {
      fromJackson(val, null, null, 0);
    }

    return val.toString();
  }

  private static int fromJackson(JSONValue val, String key, JSONObject parent, int objectId) {
    JSONObject obj;
    JSONArray arr;
    if ((obj = val.isObject()) != null) {
      obj.put(OBJECT_ID, new JSONString(new Integer(++objectId).toString()));
      
      for (String k : obj.keySet()) {
        objectId = fromJackson(obj.get(k), k, obj, objectId);
      }
    }

    if ((arr = val.isArray()) != null) {
      JSONObject arrayObject = new JSONObject();
      arrayObject.put(OBJECT_ID, new JSONString(new Integer(++objectId).toString()));
      arrayObject.put(VALUE, arr);
      parent.put(key, arrayObject);

      for (int i = 0; i < arr.size(); i++) {
        objectId = fromJackson(arr.get(i), VALUE, arrayObject, objectId);
      }
    }
    return objectId;
  }
}