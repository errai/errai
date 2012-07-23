package org.jboss.errai.jpa.client.local.backend;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.jpa.client.local.EntityJsonMatcher;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.client.local.ErraiEntityType;
import org.jboss.errai.jpa.client.local.JsonUtil;
import org.jboss.errai.jpa.client.local.Key;

import com.google.gwt.json.client.JSONObject;
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
  public native void removeAll() /*-{
    for (var i = $wnd.sessionStorage.length - 1; i >= 0; i--) {
      var key = $wnd.sessionStorage.key(i);
      $wnd.sessionStorage.removeItem(key);
    }
  }-*/;

  /**
   * Invokes the given entry visitor on each key/value pair in this entire
   * storage backend.
   *
   * @param entryVisitor
   *          The visitor that will act on each key/value pair.
   */
  private native void forEachKey(EntryVisitor entryVisitor) /*-{
    for (var i = 0, n = $wnd.sessionStorage.length; i < n; i++) {
      var key = $wnd.sessionStorage.key(i);
      var value = $wnd.sessionStorage.getItem(key);
      entryVisitor.@org.jboss.errai.jpa.client.local.backend.EntryVisitor::visit(Ljava/lang/String;Ljava/lang/String;)(key, value);
    }
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
  public <X> List<X> getAll(final ErraiEntityType<X> type, final EntityJsonMatcher matcher) {
    // TODO index entries by entity type

    final List<X> entities = new ArrayList<X>();
    forEachKey(new EntryVisitor() {
      @Override
      public void visit(String key, String value) {
        Key<?, ?> k = Key.fromJson(em, key);
        System.out.println("getAll(): considering " + value);
        if (k.getEntityType() == type) {
          System.out.println(" --> correct type");
          JSONObject candidate = JSONParser.parseStrict(value).isObject();
          Assert.notNull(candidate);
          if (matcher.matches(candidate)) {
            @SuppressWarnings("unchecked")
            Key<X, ?> typedKey = (Key<X, ?>) k;

            // Unfortunately, this throws away a lot of work we've already done (getting the entity type,
            // creating the key, doing a backend.get(), parsing the JSON value, ...)
            // it would be nice to avoid this, but we have to go back to the entity manager in case the
            // thing we want is in the persistence context.
            entities.add(em.find(type.getJavaType(), typedKey.getId()));
          }
          else {
            System.out.println(" --> but not a match");
          }
        }
        else {
          System.out.println(" --> wrong type");
        }
      }
    });
    return entities;
  }

  @Override
  public boolean contains(Key<?, ?> key) {
    String keyJson = key.toJson();
    boolean contains = getImpl(keyJson) != null;
    System.out.println("<<<contains '" + keyJson + "' : " + contains);
    return contains;
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
    JSONValue newValueJson = entityType.toJson(em, value);
    JSONValue oldValueJson = JSONParser.parseStrict(getImpl(keyJson));
    boolean modified = !JsonUtil.equals(newValueJson, oldValueJson);
    if (modified) {
      System.out.println("Detected modified entity " + key);
      System.out.println("   Old: " + oldValueJson);
      System.out.println("   New: " + newValueJson);
    }
    return modified;
  }
}
