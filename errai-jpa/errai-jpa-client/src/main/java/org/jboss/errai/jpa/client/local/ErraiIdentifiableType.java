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

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import com.google.gwt.json.client.JSONValue;

/**
 * Errai implementation of the JPA IdentifiableType metamodel interface. Specializes
 * ManagedType by adding properties related to ID and version attributes.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 *
 * @param <X> The actual type described by this metatype.
 */
public abstract class ErraiIdentifiableType<X> extends ErraiManagedType<X> implements IdentifiableType<X> {

  private SingularAttribute<? super X, ?> id;
  private SingularAttribute<? super X, ?> version;

  public ErraiIdentifiableType(Class<X> javaType) {
    super(javaType);
  }

  /**
   * Delivers the {@link PrePersist} event to the pre-persist listeners on the given
   * instance of this entity.
   *
   * @param targetEntity
   *          The entity instance to deliver the PrePersist event to.
   */
  public abstract void deliverPrePersist(X targetEntity);

  /**
   * Delivers the {@link PostPersist} event to the post-persist listeners on the given
   * instance of this entity.
   *
   * @param targetEntity
   *          The entity instance to deliver the PostPersist event to.
   */
  public abstract void deliverPostPersist(X targetEntity);

  /**
   * Delivers the {@link PreUpdate} event to the pre-Update listeners on the given
   * instance of this entity.
   *
   * @param targetEntity
   *          The entity instance to deliver the PreUpdate event to.
   */
  public abstract void deliverPreUpdate(X targetEntity);

  /**
   * Delivers the {@link PostUpdate} event to the post-Update listeners on the given
   * instance of this entity.
   *
   * @param targetEntity
   *          The entity instance to deliver the PostUpdate event to.
   */
  public abstract void deliverPostUpdate(X targetEntity);

  /**
   * Delivers the {@link PreRemove} event to the pre-Remove listeners on the given
   * instance of this entity.
   *
   * @param targetEntity
   *          The entity instance to deliver the PreRemove event to.
   */
  public abstract void deliverPreRemove(X targetEntity);

  /**
   * Delivers the {@link PostRemove} event to the post-Remove listeners on the given
   * instance of this entity.
   *
   * @param targetEntity
   *          The entity instance to deliver the PostRemove event to.
   */
  public abstract void deliverPostRemove(X targetEntity);

  /**
   * Delivers the {@link PostLoad} event to the post-load listeners on the given
   * instance of this entity.
   *
   * @param targetEntity
   *          The entity instance to deliver the PostLoad event to.
   */
  public abstract <Y> void deliverPostLoad(X targetEntity);

  /**
   * Converts the given JSONValue, which represents an instance of this entity
   * type, into the actual instance of this entity type that exists in the given
   * EntityManager's persistence context. References to other entities are
   * recursively retrieved from the EntityManager.
   *
   * @param em
   *          The EntityManager that owns this entity type and houses the
   *          persistence context.
   * @param jsonValue
   *          A value that represents an instance of this entity type.
   * @return A managed entity that is in the given EntityManager's persistence
   *         context.
   */
  @Override
  public X fromJson(EntityManager em, JSONValue jsonValue) {
    final ErraiEntityManager eem = (ErraiEntityManager) em;

    Key<X, ?> key = keyFromJson(jsonValue);

    X entity = eem.getPartiallyConstructedEntity(key);
    if (entity != null) {
      return entity;
    }

    entity = newInstance();
    try {
      eem.putPartiallyConstructedEntity(key, entity);
      for (Attribute<? super X, ?> a : getAttributes()) {
        ErraiAttribute<? super X, ?> attr = (ErraiAttribute<? super X, ?>) a;
        JSONValue attrJsonValue = jsonValue.isObject().get(attr.getName());

        // this attribute did not exist when the entity was originally persisted; skip it.
        if (attrJsonValue == null) continue;

        switch (attr.getPersistentAttributeType()) {
        case ELEMENT_COLLECTION:
        case EMBEDDED:
        case BASIC:
          parseInlineJson(entity, attr, attrJsonValue, eem);
          break;

        case MANY_TO_MANY:
        case MANY_TO_ONE:
        case ONE_TO_MANY:
        case ONE_TO_ONE:
          if (attr instanceof ErraiSingularAttribute) {
            parseSingularJsonReference(entity, (ErraiSingularAttribute<? super X, ?>) attr, attrJsonValue, eem);
          }
          else if (attr instanceof ErraiPluralAttribute) {
            parsePluralJsonReference(entity, (ErraiPluralAttribute<? super X, ?, ?>) attr, attrJsonValue.isArray(), eem);
          }
          else {
            throw new PersistenceException("Unknown attribute type " + attr);
          }
        }
      }
      return entity;
    } finally {
      eem.removePartiallyConstructedEntity(key);
    }
  }

  private Key<X, ?> keyFromJson(JSONValue json) {
    JSONValue keyJson = json.isObject().get(id.getName());
    Object idValue = JsonUtil.basicValueFromJson(keyJson, id.getJavaType());
    return new Key<X, Object>(this, idValue);
  }

  @Override
  public <Y> void addAttribute(Attribute<X, Y> attribute) {
    if (attribute instanceof SingularAttribute) {
      SingularAttribute<? super X, ?> sa = (SingularAttribute<? super X, ?>) attribute;
      if (sa.isId()) id = sa;
      if (sa.isVersion()) version = sa;
    }
    super.addAttribute(attribute);
  }

  // ---------- JPA API Below This Line -------------

  @SuppressWarnings("unchecked")
  @Override
  public <Y> ErraiSingularAttribute<? super X, Y> getId(Class<Y> type) {
    return (ErraiSingularAttribute<? super X, Y>) id;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
    // XXX the JPA spec is not clear on the difference between id and declaredId
    return (SingularAttribute<X, Y>) id;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type) {
    return (SingularAttribute<? super X, Y>) version;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type) {
    // XXX the JPA spec is not clear on the difference between version and declaredVersion
    return (SingularAttribute<X, Y>) version;
  }

  @Override
  public IdentifiableType<? super X> getSupertype() {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public boolean hasSingleIdAttribute() {
    return id != null;
  }

  @Override
  public boolean hasVersionAttribute() {
    return version != null;
  }

  @Override
  public Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public Type<?> getIdType() {
    return id.getType();
  }

  @Override
  public javax.persistence.metamodel.Type.PersistenceType getPersistenceType() {
    return PersistenceType.ENTITY;
  }

  @Override
  public String toString() {
    return "[IdentifiableType " + getJavaType().getName() + "]";
  }
}
