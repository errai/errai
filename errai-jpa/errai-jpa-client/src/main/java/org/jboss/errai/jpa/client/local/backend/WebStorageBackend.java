package org.jboss.errai.jpa.client.local.backend;

import org.jboss.errai.jpa.client.local.Key;
import org.jboss.errai.marshalling.client.Marshalling;

/**
 * The storage backend for HTML WebStorage, a storage facility supported by most
 * browsers for at least 2.5 million characters of data, (5 megabytes of unicode text).
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class WebStorageBackend implements StorageBackend {

  private native void putImpl(String key, String value) /*-{
    $wnd.sessionStorage.setItem(key, value);
  }-*/;

  private native String getImpl(String key) /*-{
    return $wnd.sessionStorage.getItem(key);
  }-*/;

  private native String removeImpl(String key) /*-{
    return $wnd.sessionStorage.removeItem(key);
  }-*/;

  @Override
  public <X> void put(Key<X, ?> key, X value) {
    String keyJson = key.toJson();
    String valueJson = Marshalling.toJSON(value);
    putImpl(keyJson, valueJson);
  }

  @Override
  public <X> X get(Key<X, ?> key) {
    String keyJson = key.toJson();
    String valueJson = getImpl(keyJson);
    if (valueJson == null) {
      return null;
    }
    return Marshalling.fromJSON(valueJson, key.getEntityType().getJavaType());
  }

  @Override
  public <X> void remove(Key<X, ?> key) {
    String keyJson = key.toJson();
    removeImpl(keyJson);
  }

  @Override
  public <X> boolean isModified(Key<X, ?> key, X value) {
    String keyJson = key.toJson();
    String valueJson = Marshalling.toJSON(value);
    return !valueJson.equals(getImpl(keyJson));
  }

}
