package org.jboss.errai.js.client.bus.marshall;

import com.google.gwt.core.client.JavaScriptObject;

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
        v = @org.jboss.errai.js.client.bus.marshall.MsgTools::listMarshall([Ljava/lang/Object;)(v);
      }
      else if (!(typeof v == "string" || typeof v == "number")) {
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
}
