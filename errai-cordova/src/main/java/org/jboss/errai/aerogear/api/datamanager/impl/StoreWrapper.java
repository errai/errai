package org.jboss.errai.aerogear.api.datamanager.impl;

import com.google.gwt.core.client.JavaScriptObject;
import org.jboss.errai.aerogear.api.datamanager.Store;
import org.jboss.errai.aerogear.api.datamanager.StoreType;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author edewit@redhat.com
 */
public class StoreWrapper<T> implements Store<T> {

  @Override
  public StoreType getType() {
    return StoreType.valueOf(getType0());
  }

  private native String getType0() /*-{
      return $wnd.store.type;
  }-*/;

  @Override
  public Collection<T> readAll() {
    JsArray<T> jsArray = readAll0();
    HashSet<T> result = new HashSet<T>(jsArray.length());

    for (int i = 0; i < jsArray.length(); i++) {
       result.add(jsArray.get(i));
    }
    return result;
  }

  private native JsArray<T> readAll0() /*-{
      return $wnd.store.read();
  }-*/;

  @Override
  public T read(Serializable id) {
    return read0(id).get(0);
  }

  private native JsArray<T> read0(Serializable id) /*-{
      return $wnd.store.read(id);
  }-*/;

  @Override
  public void save(T item) {
    save0(item);
  }

  private native void save0(T item) /*-{
    $wnd.store.save(item);
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

  static class JsArray<T> extends JavaScriptObject {
    protected JsArray() { }
    public final native int length() /*-{ return this.length; }-*/;
    public final native T get(int i) /*-{ return this[i];     }-*/;
  }
}
