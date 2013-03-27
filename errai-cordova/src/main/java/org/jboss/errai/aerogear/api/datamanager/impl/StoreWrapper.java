package org.jboss.errai.aerogear.api.datamanager.impl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import org.jboss.errai.aerogear.api.datamanager.Store;
import org.jboss.errai.aerogear.api.datamanager.StoreType;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author edewit@redhat.com
 */
public class StoreWrapper<T> implements Store<T> {
  static {
    MarshallerFramework.initializeDefaultSessionProvider();
  }

  @Override
  public StoreType getType() {
    return StoreType.valueOf(getType0());
  }

  private native String getType0() /*-{
      return $wnd.store.type;
  }-*/;

  @Override
  public Collection<T> readAll() {
    JsArray jsArray = readAll0();
    HashSet<T> result = new HashSet<T>(jsArray.length());

    for (int i = 0; i < jsArray.length(); i++) {
      result.add(fromJSON(toJSON(jsArray.get(i))));
    }
    return result;
  }

  private native JsArray readAll0() /*-{
      return $wnd.store.read();
  }-*/;

  @Override
  public T read(Serializable id) {
    if (id instanceof Number) {
      return fromJSON(toJSON(read0((Integer) id)));
    }
    return fromJSON(toJSON(read0(id)));
  }

  private String toJSON(JavaScriptObject object) {
    return new JSONObject(object).toString();
  }

  private T fromJSON(String json) {
    return (T) Marshalling.fromJSON(json);
  }

  private native JavaScriptObject read0(Integer id) /*-{
      return $wnd.store.read(Number(id))[0];
  }-*/;

  private native JavaScriptObject read0(Serializable id) /*-{
      return $wnd.store.read(id)[0];
  }-*/;

  @Override
  public void save(T item) {
    save0(Marshalling.toJSON(item));
  }

  private native void save0(String item) /*-{
      $wnd.store.save(eval('[' + item + ']'));
  }-*/;

  @Override
  public void reset() {
  }

  @Override
  public void remove(Serializable id) {
    remove0(id);
  }

  private native void remove0(Serializable id) /*-{
      $wnd.store.remove(id);
  }-*/;
}
