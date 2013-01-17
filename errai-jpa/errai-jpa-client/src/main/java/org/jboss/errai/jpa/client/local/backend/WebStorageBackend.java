package org.jboss.errai.jpa.client.local.backend;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.Assert;
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

  @Override
  public void removeAll() {
    LocalStorage.removeAll();
  }

  @Override
  public <X> void put(Key<X,?> key, X value) {
    ErraiEntityType<X> entityType = key.getEntityType();
    String keyJson = key.toJson();
    JSONValue valueJson = entityType.toJson(em, value);
    System.out.println(">>>put '" + keyJson + "'");
    LocalStorage.put(keyJson, valueJson.toString());
  }

  @Override
  public <X> X get(Key<X, ?> key) {
    ErraiEntityType<X> entityType = key.getEntityType();
    String keyJson = key.toJson();
    String valueJson = LocalStorage.get(keyJson);
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
    LocalStorage.forEachKey(new EntryVisitor() {
      @Override
      public void visit(String key, String value) {
        Key<?, ?> k = Key.fromJson(em, key, false);
        if (k == null) return;
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
    boolean contains = LocalStorage.get(keyJson) != null;
    System.out.println("<<<contains '" + keyJson + "' : " + contains);
    return contains;
  }

  @Override
  public <X> void remove(Key<X, ?> key) {
    String keyJson = key.toJson();
    LocalStorage.remove(keyJson);
  }

  @Override
  public <X> boolean isModified(Key<X, ?> key, X value) {
    ErraiEntityType<X> entityType = key.getEntityType();
    String keyJson = key.toJson();
    JSONValue newValueJson = entityType.toJson(em, value);
    JSONValue oldValueJson = JSONParser.parseStrict(LocalStorage.get(keyJson));
    boolean modified = !JsonUtil.equals(newValueJson, oldValueJson);
    if (modified) {
      System.out.println("Detected modified entity " + key);
      System.out.println("   Old: " + oldValueJson);
      System.out.println("   New: " + newValueJson);
    }
    return modified;
  }
}
