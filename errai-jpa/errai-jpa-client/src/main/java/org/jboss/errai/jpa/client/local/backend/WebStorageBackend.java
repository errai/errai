package org.jboss.errai.jpa.client.local.backend;

/**
 * The storage backend for HTML WebStorage, a storage facility supported by most
 * browsers for at least 2.5 million characters of data.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class WebStorageBackend implements StorageBackend {

  @Override
  public native void put(String key, String value) /*-{
    $wnd.localStorage.setItem(key, value);
  }-*/;

  @Override
  public native String get(String key) /*-{
    return $wnd.localStorage.getItem(key);
  }-*/;

}
