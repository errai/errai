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

package org.jboss.errai.jpa.client.local;

import org.jboss.errai.common.client.api.Assert;

import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * Holder class for a storage key: a tuple of type and identity value.
 * <p>
 * Instances of this class are immutable.
 *
 * @param <X> The entity's Java type
 * @param <T> The entity's identity type
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class Key<X, T> {

  private final ErraiManagedType<X> entityType;
  private final T id;

  /**
   * Creates a key that uniquely identifies an entity of a particular type (the
   * entity with this identity does not necessarily exist, but if it did, this
   * would be its identity).
   *
   * @param entityType The type of the entity. Must not be null.
   * @param id The ID of the entity. Must not be null.
   * @throws NullPointerException if either argument is null.
   */
  public Key(ErraiManagedType<X> entityType, T id) {
    this.entityType = Assert.notNull(entityType);
    this.id = Assert.notNull(id);
  }

  /**
   * Returns a Key instance for the entity type of the given class.
   *
   * @param em
   *          The entity manager (required for looking up the EntityType for the
   *          given class). Must not be null.
   * @param entityClass
   *          The class of the entity for the key. Must not be null.
   * @param id
   *          The ID value for the entity. Must not be null.
   * @return A Key instance for the given entity type and ID value.
   * @throws NullPointerException
   *           if any argument is null.
   * @throws IllegalArgumentException
   *           if {@code entityClass} is not a known JPA entity type.
   */
  public static <X, T> Key<X, T> get(ErraiEntityManager em, Class<X> entityClass, T id) {
    ErraiIdentifiableType<X> entityType = em.getMetamodel().entity(entityClass);
    return new Key<X, T>(entityType, id);
  }

  /**
   * Returns the managed type this key refers to.
   *
   * @return the entity type of the key. Never null.
   */
  public ErraiManagedType<X> getEntityType() {
    return entityType;
  }

  /**
   * Returns the ID value for this key.
   *
   * @return the ID value of the key. Never null.
   */
  public T getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
            + ((entityType == null) ? 0 : entityType.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Key<?, ?> other = (Key<?, ?>) obj;
    if (entityType == null) {
      if (other.entityType != null)
        return false;
    }
    else if (!entityType.equals(other.entityType))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Key [entityType=" + entityType + ", id=" + id + "]";
  }

  /**
   * Returns a JSON string representation of this key which can be turned back
   * into a Key instance via
   * {@link Key#fromJson(ErraiEntityManager, String, boolean)}.
   *
   * @see #toJsonObject()
   * @return a JSON-formatted string. Never null.
   */
  public String toJson() {
    return toJsonObject().toString();
  }

  /**
   * Returns a JSONObject representation of this key which can be turned back
   * into a Key instance via
   * {@link Key#fromJsonObject(ErraiEntityManager, JSONObject, boolean)}.
   *
   * @see #toJson()
   * @return a JSON object that represents this key. Never null.
   */
  public JSONObject toJsonObject() {
    JSONObject keyJson = new JSONObject();
    keyJson.put("entityType", new JSONString(entityType.getJavaType().getName()));
    keyJson.put("id", JsonUtil.basicValueToJson(id));
    return keyJson;
  }

  /**
   * Returns a Key instance based on the given JSON string.
   *
   * @param em
   *          The entity manager that can be used to look up the entity type
   *          corresponding with the key.
   * @param key
   *          The key to parse.
   * @param failIfNotFound
   *          If true, and the entity type given in {@code key} is not known to
   *          {@code em}, an IllegalArgumentException will be thrown.
   * @return An instance of Key that corresponds with the entity type and ID of
   *         the given JSON string.
   */
  public static Key<?, ?> fromJson(ErraiEntityManager em, String key, boolean failIfNotFound) {
    JSONValue k;
    try {
      k = JSONParser.parseStrict(key);

    } catch (JSONException e) {
      throw new JSONException("Input: " + key, e);
    }
    return fromJsonObject(em, k.isObject(), failIfNotFound);
  }

  /**
   * Returns a Key instance based on the given JSON object.
   *
   * @param em
   *          The entity manager that can be used to look up the entity type
   *          corresponding with the key.
   * @param key
   *          The properties of the key to create.
   * @param failIfNotFound
   *          If true, and the entity type given in {@code key} is not known to
   *          {@code em}, an IllegalArgumentException will be thrown.
   * @return An instance of Key that corresponds with the entity type and ID of
   *         the given JSON object.
   */
  public static Key<?, ?> fromJsonObject(ErraiEntityManager em, JSONObject key, boolean failIfNotFound) {
    String entityClassName = key.get("entityType").isString().stringValue();
    ErraiIdentifiableType<Object> et = em.getMetamodel().entity(entityClassName, failIfNotFound);
    if (et == null) {
      return null;
    }
    ErraiSingularAttribute<?, Object> idAttr = et.getId(Object.class);
    Object id = JsonUtil.basicValueFromJson(key.get("id"), idAttr.getJavaType());

    return new Key<Object, Object>(et, id);
  }
}
