/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.js.client.bus.marshall;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock
 */
public abstract class MsgTools {
  public static JavaScriptObject mapToJSPrototype(Map<String, Object> partsMap) {
    return unwrapMap(partsMap.keySet().toArray(new String[partsMap.size()]), partsMap);
  }

  public static Map<String, Object> jsObjToMap(Object o) {
    return wrapMap(o);
  }

  private static native JavaScriptObject unwrapMap(String[] keys, Map<String, Object> partsMap) /*-{
    var newProto = new Object();

    for (var i = 0; i < keys.length; i++) {
      newProto[keys[i]] = partsMap.@java.util.Map::get(Ljava/lang/Object;)(keys[i]);
    }

    return newProto;
  }-*/;

  @SuppressWarnings("GwtJavaFromJSMethodCalls")
  private static native Map<String, Object> wrapMap(Object object) /*-{
    var map = @java.util.HashMap::new()();

    for (var item in object) {
      var v = object[item];

      if ($wnd.erraiTypeOf(v) == "array") {
        if (object["^EncodedType"] == "[Ljava.lang.Object;") {
          v = @org.jboss.errai.js.client.bus.marshall.MsgTools::objectMarshall(Ljava/lang/Object;)(v);
        }
        else {
          v = @org.jboss.errai.js.client.bus.marshall.MsgTools::listMarshall([Ljava/lang/Object;)(v);
        }
      }
      else if (!(typeof v == "string" || typeof v == "number" || typeof v == "object")) {
        v = @org.jboss.errai.js.client.bus.marshall.MsgTools::wrapMap(Ljava/lang/Object;)(v);
      }

      map.@java.util.HashMap::put(Ljava/lang/Object;Ljava/lang/Object;)(item, v);
    }

    return map;
  }-*/;

  private static Object listMarshall(Object[] oArray) {
    List<Object> list = new ArrayList<Object>();
    for (Object o : oArray) {
      if (o.getClass().isArray()) {
        list.add(listMarshall((Object[]) o));
      }
      else {
        list.add(o);
      }
    }
    return list;
  }
  
  public static Object[] objectMarshall(Object o) {
    JsArray oArray = (JsArray) o;
    Object[] array = new Object[oArray.length()];
    for (int i = 0; i < array.length; i++) {
      array[i] = oArray.get(i);
    }
    return array;
  }
}
