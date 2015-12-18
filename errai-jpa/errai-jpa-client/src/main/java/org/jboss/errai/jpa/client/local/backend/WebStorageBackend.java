/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.client.local.backend;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.jpa.client.local.EntityJsonMatcher;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.client.local.ErraiIdentifiableType;
import org.jboss.errai.jpa.client.local.ErraiManagedType;
import org.jboss.errai.jpa.client.local.JsonUtil;
import org.jboss.errai.jpa.client.local.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * The storage backend for HTML WebStorage, a storage facility supported by most
 * browsers for at least 2.5 million characters of data, (5 megabytes of Unicode
 * text).
 * <p>
 * This backend supports <i>namespacing</i>, which is a way of dividing up the
 * storage into any number of non-overlapping buckets. For any two namespaces
 * <i>A</i> and <i>B</i> (<i>A</i> != <i>B</i>), the storage backend for
 * namespace <i>A</i> will never see, modify, or otherwise or interfere with
 * anything stored in the storage backend for namespace <i>B</i>.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class WebStorageBackend implements StorageBackend {

  public static final StorageBackendFactory FACTORY = new StorageBackendFactory() {
    @Override
    public StorageBackend createInstanceFor(ErraiEntityManager em) {
      return new WebStorageBackend(em);
    }
  };

  private final ErraiEntityManager em;
  private final String namespace;
  
  private final Logger logger;

  /**
   * Creates a WebStorageBackend that works with entities in the default storage
   * namespace.
   *
   * @param erraiEntityManager
   *          the ErraiEntityManager this storage backend will be used with (it
   *          is used for resolving entity references).
   */
  public WebStorageBackend(ErraiEntityManager erraiEntityManager) {
    this(erraiEntityManager, "");
  }

  /**
   * Creates a WebStorageBackend that works with entities in the given storage
   * namespace.
   *
   * @param erraiEntityManager
   *          the ErraiEntityManager this storage backend will be used with (it
   *          is used for resolving entity references).
   * @param namespace
   *          The namespace to operate within. Must not be null.
   */
  public WebStorageBackend(ErraiEntityManager erraiEntityManager, String namespace) {
    em = Assert.notNull(erraiEntityManager);
    this.namespace = Assert.notNull(namespace);
    this.logger = LoggerFactory.getLogger(WebStorageBackend.class);
  }

  @Override
  public void removeAll() {

    // this is done in two phases because it would be bad to modify the key set while iterating over it
    final List<String> toRemove = new ArrayList<String>();

    LocalStorage.forEachKey(new EntryVisitor() {
      @Override
      public void visit(String key, String value) {
        if (parseNamespacedKey(em, key, false) != null) {
          toRemove.add(key);
        }
      }
    });

    for (String key : toRemove) {
      LocalStorage.remove(key);
    }
  }

  @Override
  public <X> void put(Key<X,?> key, X value) {
    ErraiManagedType<X> entityType = key.getEntityType();
    String keyJson = namespace + key.toJson();
    JSONValue valueJson = entityType.toJson(em, value);
    logger.trace(">>>put '" + keyJson + "'");
    LocalStorage.put(keyJson, valueJson.toString());
  }

  @Override
  public <X> X get(Key<X, ?> requestedKey) {
    for (ErraiManagedType<? extends X> entityType : requestedKey.getEntityType().getSubtypes()) {
      Key<X, ?> key = new Key<X, Object>((ErraiManagedType<X>) entityType, (Object) requestedKey.getId());
      String keyJson = namespace + key.toJson();
      String valueJson = LocalStorage.get(keyJson);
      logger.trace("<<<get '" + keyJson + "' : " + valueJson);
      X entity;
      if (valueJson != null) {
        entity = entityType.fromJson(em, JSONParser.parseStrict(valueJson));
        logger.trace("   returning " + entity);
        return entity;
      }
    }
    return null;
  }

  @Override
  public <X> List<X> getAll(final ErraiIdentifiableType<X> type, final EntityJsonMatcher matcher) {
    // TODO index entries by entity type

    final List<X> entities = new ArrayList<X>();
    LocalStorage.forEachKey(new EntryVisitor() {
      @Override
      public void visit(String key, String value) {
        Key<?, ?> k = parseNamespacedKey(em, key, false);
        if (k == null) return;
        logger.trace("getAll(): considering " + value);
        if (type.isSuperclassOf(k.getEntityType())) {
          logger.trace(" --> correct type");
          JSONObject candidate = JSONParser.parseStrict(value).isObject();
          Assert.notNull(candidate);
          if (matcher.matches(candidate)) {
            @SuppressWarnings("unchecked")
            Key<X, ?> typedKey = (Key<X, ?>) k;

            // Unfortunately, this throws away a lot of work we've already done (getting the entity type,
            // creating the key, doing a backend.get(), parsing the JSON value, ...)
            // it would be nice to avoid this, but we have to go back to the entity manager in case the
            // thing we want is in the persistence context.
            entities.add((X) em.find(k.getEntityType().getJavaType(), typedKey.getId()));
          }
          else {
            logger.trace(" --> but not a match");
          }
        }
        else {
          logger.trace(" --> wrong type");
        }
      }
    });
    return entities;
  }

  @Override
  public <X, Y> boolean contains(Key<X, Y> key) {
    boolean contains = false;
    for (ErraiManagedType<X> type : key.getEntityType().getSubtypes()) {
      Key<?, ?> k = new Key<X, Y>(type, key.getId());
      String keyJson = namespace + k.toJson();
      contains = LocalStorage.get(keyJson) != null;
      logger.trace("<<<contains '" + keyJson + "' : " + contains);
      if (contains) break;
    }
    return contains;
  }

  @Override
  public <X> void remove(Key<X, ?> key) {
    String keyJson = namespace + key.toJson();
    LocalStorage.remove(keyJson);
  }

  @Override
  public <X> boolean isModified(Key<X, ?> key, X value) {
    ErraiManagedType<X> entityType = key.getEntityType();
    String keyJson = namespace + key.toJson();
    JSONValue newValueJson = entityType.toJson(em, value);
    JSONValue oldValueJson = JSONParser.parseStrict(LocalStorage.get(keyJson));
    boolean modified = !JsonUtil.equals(newValueJson, oldValueJson);
    if (modified) {
      logger.trace("Detected modified entity " + key);
      logger.trace("   Old: " + oldValueJson);
      logger.trace("   New: " + newValueJson);
    }
    return modified;
  }

  private Key<?, ?> parseNamespacedKey(ErraiEntityManager em, String key, boolean failIfNotFound) {
    if ( (!key.startsWith(namespace)) || namespace.length() >= key.length()) return null;
    key = key.substring(namespace.length());
    if (key.charAt(0) != '{') return null;
    return Key.fromJson(em, key, failIfNotFound);
  }
}
