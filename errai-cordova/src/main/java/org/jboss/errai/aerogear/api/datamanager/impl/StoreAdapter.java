package org.jboss.errai.aerogear.api.datamanager.impl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import org.jboss.errai.aerogear.api.datamanager.Store;
import org.jboss.errai.aerogear.api.datamanager.StoreType;
import org.jboss.errai.aerogear.api.impl.AbstractAdapter;
import org.jboss.errai.marshalling.client.Marshalling;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author edewit@redhat.com
 */
public class StoreAdapter<T> extends AbstractAdapter<T> implements Store<T> {
  public StoreAdapter(JavaScriptObject store) {
    this.object = store;
  }

  @Override
  public StoreType getType() {
    return StoreType.valueOf(getType0());
  }

  private native String getType0() /*-{
      return this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.type;
  }-*/;

  @Override
  public Collection<T> readAll() {
    return convertToType(readAll0());
  }

  private native JsArray readAll0() /*-{
      return this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.read();
  }-*/;

  @Override
  public T read(Serializable id) {
    if (id instanceof Number) {
      return convertToType(read0((Integer) id));
    }
    return convertToType(read0(id));
  }

  private native JavaScriptObject read0(Integer id) /*-{
      return this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.read(Number(id))[0];
  }-*/;

  private native JavaScriptObject read0(Serializable id) /*-{
      return this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.read(id)[0];
  }-*/;

  @Override
  public void save(T item) {
    save0(Marshalling.toJSON(item));
  }

  private native void save0(String item) /*-{
      this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.save(eval('[' + item + ']'));
  }-*/;

  @Override
  public void reset() {
  }

  @Override
  public void remove(Serializable id) {
    remove0(id);
  }

  private native void remove0(Serializable id) /*-{
      this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.remove(id);
  }-*/;
}
