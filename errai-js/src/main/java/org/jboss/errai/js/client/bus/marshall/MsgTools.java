package org.jboss.errai.js.client.bus.marshall;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.Map;

/**
 * @author Mike Brock
 */
public abstract class MsgTools {
  public static JavaScriptObject mapToJSPrototype(Map<String, Object> partsMap) {
    return unwrapMap(partsMap.keySet().toArray(new String[partsMap.size()]), partsMap);
  }
  
  private static native JavaScriptObject unwrapMap(String[] keys, Map<String, Object> partsMap) /*-{

    var newProto = new Object();

    for (var i = 0; i < keys.length; i++) {
      newProto[keys[i]] =  partsMap.@java.util.Map::get(Ljava/lang/Object;)(keys[i]);
    }

    return newProto;
  }-*/;
}
