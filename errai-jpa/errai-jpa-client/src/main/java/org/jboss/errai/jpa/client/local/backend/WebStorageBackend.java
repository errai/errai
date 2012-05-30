package org.jboss.errai.jpa.client.local.backend;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.client.local.ErraiEntityType;
import org.jboss.errai.jpa.client.local.Key;

import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * The storage backend for HTML WebStorage, a storage facility supported by most
 * browsers for at least 2.5 million characters of data, (5 megabytes of unicode text).
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class WebStorageBackend implements StorageBackend {

  private final ErraiEntityManager em;

  public WebStorageBackend(ErraiEntityManager erraiEntityManager) {
    em = Assert.notNull(erraiEntityManager);
  }

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
  public <X> void put(Key<X,?> key, X value) {
    ErraiEntityType<X> entityType = key.getEntityType();
    String keyJson = key.toJson();
    JSONValue valueJson = entityType.toJson(em, value);
    System.out.println(">>>put '" + keyJson + "'");
    putImpl(keyJson, valueJson.toString());
  }

  @Override
  public <X> X get(Key<X, ?> key) {
    ErraiEntityType<X> entityType = key.getEntityType();
    String keyJson = key.toJson();
    String valueJson = getImpl(keyJson);
    System.out.println("<<<get '" + keyJson + "' : " + valueJson);
    X entity;
    if (valueJson == null) {
      entity = null;
    }
    else {
      entity = entityType.fromJson(em, JSONParser.parseStrict(valueJson));
    }
    System.out.println("   returning " + entity);
    return entity;
  }

  @Override
  public <X> void remove(Key<X, ?> key) {
    String keyJson = key.toJson();
    removeImpl(keyJson);
  }

  @Override
  public <X> boolean isModified(Key<X, ?> key, X value) {
    ErraiEntityType<X> entityType = key.getEntityType();
    String keyJson = key.toJson();
    String valueJson = entityType.toJson(em, value).toString();
    return !valueJson.equals(getImpl(keyJson));
  }

}
