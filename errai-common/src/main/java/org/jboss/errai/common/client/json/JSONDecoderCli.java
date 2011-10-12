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

package org.jboss.errai.common.client.json;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.common.client.types.DecodingContext;
import org.jboss.errai.common.client.types.DataTypeHelper;
import org.jboss.errai.common.client.types.UHashMap;
import org.jboss.errai.common.client.types.UnsatisfiedForwardLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class JSONDecoderCli {
  public static Object decode(Object value) {
    DecodingContext ctx = new DecodingContext();
    Object v = null;

    if (value instanceof String) {
      v = _decode(JSONParser.parseStrict((String) value), ctx);
    }
    else if (value instanceof JSONValue) {
      v = _decode((JSONValue) value, ctx);
    }
    else if (value != null) {
      throw new RuntimeException("could not decode type: " + value.getClass());
    }

    if (ctx.isUnsatisfiedDependencies()) {
      DataTypeHelper.resolveDependencies(ctx);
    }

    return v;
  }

  public static Object decode(Object value, DecodingContext ctx) {
    if (value instanceof String) {
      return _decode(JSONParser.parseStrict((String) value), ctx);
    }
    else if (value instanceof JSONValue) {
      return _decode((JSONValue) value, ctx);
    }
    else if (value != null) {
      throw new RuntimeException("could not decode type: " + value.getClass());
    }

    return null;
  }

  private static Object _decode(JSONValue v, DecodingContext ctx) {
    if (v.isString() != null) {
      return v.isString().stringValue();
    }
    else if (v.isNumber() != null) {
      return v.isNumber().doubleValue();
    }
    else if (v.isBoolean() != null) {
      return v.isBoolean().booleanValue();
    }
    else if (v.isNull() != null) {
      return null;
    }
    else if (v instanceof JSONObject) {
      return decodeObject(v.isObject(), ctx);
    }
    else if (v instanceof JSONArray) {
      return decodeList(v.isArray(), ctx);
    }
    else {
      throw new RuntimeException("unknown encoding");
    }
  }

  private static Object decodeObject(JSONObject eMap, DecodingContext ctx) {
    String className = eMap.get(SerializationParts.ENCODED_TYPE).isString().stringValue();
    if (DataTypeHelper.getMarshallerProvider().hasMarshaller(className)) {
      try {
        return DataTypeHelper.getMarshallerProvider().demarshall(className, eMap);
      }
      catch (Throwable t) {
        t.printStackTrace();
        GWT.log("Failure decoding object", t);
        return null;
      }
    }
    else {
      GWT.log("Could not demartial class: " + className + "; There is no available demarshaller. " +
              "Ensure you have exposed the class with @ExposeEntity.", null);
      throw new RuntimeException("no available demarshaller: " + className);
    }
  }

  private static List<Object> decodeList(JSONArray arr, DecodingContext ctx) {
    List<Object> list = new ArrayList<Object>();
    Object o;
    for (int i = 0; i < arr.size(); i++) {
      if ((o = _decode(arr.get(i), ctx)) instanceof UnsatisfiedForwardLookup) {
        ctx.addUnsatisfiedDependency(list, ((UnsatisfiedForwardLookup) o));
      }
      else {
        list.add(o);
      }
    }
    return list;
  }
}
