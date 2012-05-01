package org.jboss.errai.jpa.client.local.backend;

import org.jboss.errai.jpa.client.local.ErraiEntityType;
import org.jboss.errai.marshalling.client.Marshalling;

/**
 * The storage backend for HTML WebStorage, a storage facility supported by most
 * browsers for at least 2.5 million characters of data.
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

  @Override
  public <X, T> void put(ErraiEntityType<X> type, T id, X value) {
    String keyJson = makeKey(type, id);
    String valueJson = Marshalling.toJSON(value);
    System.out.println("Storing.\nKey=" + keyJson + "\nValue=" + valueJson);
    putImpl(keyJson, valueJson);
  }

  @Override
  public <X, T> X get(ErraiEntityType<X> type, T id) {
    String keyJson = makeKey(type, id);
    String valueJson = getImpl(keyJson);
    if (valueJson == null) {
      return null;
    }
    return Marshalling.fromJSON(valueJson, type.getJavaType());
  }

  private String makeKey(ErraiEntityType<?> type, Object id) {
    return "{ entityType: \"" + type.getJavaType().getName()
            + "\", key: " + Marshalling.toJSON(id) + "}";
  }
}
