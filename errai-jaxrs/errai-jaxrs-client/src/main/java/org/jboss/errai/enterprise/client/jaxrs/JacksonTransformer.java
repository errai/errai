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

  public String toJackson(String erraiJson) {
    JSONValue val = JSONParser.parseStrict(erraiJson);
    if (val.isObject() != null) {
      toJackson(val, null, null);
    }

    return val.toString();
  }

  public String fromJackson(String jackson) {
    JSONValue val = JSONParser.parseStrict(jackson);
    if (val.isObject() != null) {
      fromJackson(val, null, null);
    }

    return val.toString();
  }

  int i;
  private void fromJackson(JSONValue val, JSONObject parent, String key) {
    JSONObject obj;
    JSONArray arr;
    if ((obj = val.isObject()) != null) {
      obj.put("^ObjectID", new JSONString(new Integer(++i).toString()));

      for (String k : obj.keySet()) {
        fromJackson(obj.get(k), obj, k);
      }
    }

    if ((arr = val.isArray()) != null) {
      JSONObject arrayObject = new JSONObject();
      arrayObject.put("^ObjectID", new JSONString(new Integer(++i).toString()));
      arrayObject.put("^Value", arr);
      parent.put(key, arrayObject);

      for (int i = 0; i < arr.size(); i++) {
        fromJackson(arr.get(i), arrayObject, "^Value");
      }
    }
  }

  Map<String, JSONValue> backReferences = new HashMap<String, JSONValue>();
  private void toJackson(JSONValue val, JSONObject parent, String key) {
    JSONObject obj;
    JSONArray arr;
    if ((obj = val.isObject()) != null) {
      JSONValue backRef = backReferences.get(obj.get("^ObjectID").toString());
      if (backRef != null) {
        parent.put(key, backRef);
      } else {
       backReferences.put(obj.get("^ObjectID").toString(), obj);
      }
        
      obj.put("^ObjectID", null);
      obj.put("^EncodedType", null);

      for (String k : obj.keySet()) {
        if ((arr = obj.get(k).isArray()) != null) {
          for (int i = 0; i < arr.size(); i++) {
            toJackson(arr.get(i), obj, "^Value");
          }
          parent.put(key, obj.get(k));
        }
        toJackson(obj.get(k), obj, k);
      }
    }
  }
}