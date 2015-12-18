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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.errai.common.client.api.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 * Errai implementation of the JPA ManagedType metamodel interface. Defines the
 * attributes common to all managed types (which are entity, mapped superclass,
 * and embeddable types).
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 *
 * @param <X>
 *          The actual type described by this metatype.
 */
public abstract class ErraiManagedType<X> implements ManagedType<X> {

  private final Set<SingularAttribute<? super X, ?>> singularAttributes = new HashSet<SingularAttribute<? super X,?>>();
  private final Set<PluralAttribute<? super X, ?, ?>> pluralAttributes = new HashSet<PluralAttribute<? super X, ?, ?>>();

  protected final Class<X> javaType;
  private Collection<ErraiManagedType<X>> subtypes = new HashSet<ErraiManagedType<X>>();
  
  private final Logger logger;

  public ErraiManagedType(Class<X> javaType) {
    this.javaType = javaType;
    this.logger = LoggerFactory.getLogger(ErraiManagedType.class);
  }

  public <Y> void addAttribute(Attribute<X, Y> attribute) {
    if (attribute instanceof SingularAttribute) {
      SingularAttribute<? super X, ?> sa = (SingularAttribute<? super X, ?>) attribute;
      singularAttributes.add(sa);
    }
    else if (attribute instanceof PluralAttribute) {
      @SuppressWarnings("unchecked")
      PluralAttribute<? super X, ?, ?> pa = (PluralAttribute<? super X, ?, ?>) attribute;
      pluralAttributes.add(pa);
    }
    else {
      assert (false) : "Unknown attribute type " + attribute;
    }
  }

  /**
   * Creates and returns a new instance of the represented type. This
   * implementation always throws an exception; subclasses that represent
   * instantiable types should override this method with one that creates a new
   * instance of that type.
   *
   * @return a new instance of type X.
   * @throws UnsupportedOperationException
   *           if the represented type is abstract.
   */
  public X newInstance() {
    // subclasses override when this operation is possible
    throw new RuntimeException("Can't create an instance of " + getJavaType().getName());
  }

  /**
   * Returns true if this managed type represents the same Java class or a
   * superclass of the given type.
   *
   * @param other
   *          the ManagedType to check
   * @return true if the Java type of this managed is a superclass of the Java
   *         type of the other managed type.
   */
  public boolean isSuperclassOf(ManagedType<?> other) {
    Class<?> myClass = getJavaType();
    Class<?> otherClass = other.getJavaType();
    while (otherClass != null) {
      if (myClass == otherClass) {
        return true;
      }
      otherClass = otherClass.getSuperclass();
    }
    return false;
  }

  /**
   * Returns the collection of entity types that are subclasses of this managed
   * type. The returned collection includes this type itself (the trivial
   * subtype)!
   *
   * @return
   */
  public Collection<ErraiManagedType<X>> getSubtypes() {
    return subtypes;
  }

  /**
   * Only intended for use by the generated code that bootstraps Errai JPA.
   */
  @SuppressWarnings("unchecked")
  void addSubtype(ErraiManagedType<? extends X> subtype) {
    subtypes.add((ErraiManagedType<X>) subtype);
  }

  /**
   * Converts the given JSONValue, which represents an instance of this entity
   * type, into the actual instance of this entity type that exists in the given
   * EntityManager's persistence context. References to other identifiable
   * objects are recursively retrieved from the EntityManager.
   *
   * @param em
   *          The EntityManager that owns this entity type and houses the
   *          persistence context.
   * @param jsonValue
   *          A value that represents an instance of this entity type.
   * @return A managed entity that is in the given EntityManager's persistence
   *         context.
   */
  public abstract X fromJson(EntityManager em, JSONValue jsonValue);

  public JSONValue toJson(EntityManager em, X sourceEntity) {
    final ErraiEntityManager eem = (ErraiEntityManager) em;
    JSONObject jsonValue = new JSONObject();

    for (Attribute<? super X, ?> a : getAttributes()) {
      ErraiAttribute<? super X, ?> attr = (ErraiAttribute<? super X, ?>) a;
      switch (attr.getPersistentAttributeType()) {
      case ELEMENT_COLLECTION:
      case EMBEDDED:
      case BASIC:
        jsonValue.put(attr.getName(), makeInlineJson(sourceEntity, attr, eem));
      break;

      case MANY_TO_MANY:
      case MANY_TO_ONE:
      case ONE_TO_MANY:
      case ONE_TO_ONE:
        JSONValue attributeValue;
        if (attr instanceof ErraiSingularAttribute) {
          attributeValue = makeJsonReference(sourceEntity, (ErraiSingularAttribute<? super X, ?>) attr, eem);
        }
        else if (attr instanceof ErraiPluralAttribute) {
          attributeValue = makeJsonReference(sourceEntity, (ErraiPluralAttribute<? super X, ?, ?>) attr, eem);
        }
        else {
          throw new PersistenceException("Unknown attribute type " + attr);
        }
        jsonValue.put(attr.getName(), attributeValue);
      }
    }

    return jsonValue;
  }

  /**
   * Copies the state of the attributes in sourceEntity into targetEntity.
   * Related entities are resolved from the given entity manager before the
   * state is copied.
   *
   * @param em
   *          The entity manager that sourceEntity and targetEntity exist in.
   * @param targetEntity
   *          The entity whose attributes' state will be written to. Not null.
   * @param sourceEntity
   *          The entity whose attributes' state will be read from. Not null.
   */
  public void mergeState(ErraiEntityManager em, X targetEntity, X sourceEntity) {
    for (Attribute<? super X, ?> a : getAttributes()) {
      ErraiAttribute<? super X, ?> attr = (ErraiAttribute<? super X, ?>) a;
      switch (attr.getPersistentAttributeType()) {
      case ELEMENT_COLLECTION:
      case EMBEDDED:
      case BASIC:
        copyAttribute(attr, targetEntity, sourceEntity);
        break;

      case MANY_TO_MANY:
      case ONE_TO_MANY:
        copyPluralAssociation(em, ((ErraiPluralAttribute<X, ?, ?>) attr), targetEntity, sourceEntity);
        break;

      case MANY_TO_ONE:
      case ONE_TO_ONE:
        copySingularAssociation(em, attr, targetEntity, sourceEntity);
        break;
      default:
        throw new IllegalArgumentException("Attribute has unknown type: " + attr);
      }
    }
  }

  private static <X, Y> void copyAttribute(ErraiAttribute<X, Y> attr, X targetEntity, X sourceEntity) {
    attr.set(targetEntity, attr.get(sourceEntity));
  }

  private static <X, Y> void copySingularAssociation(
          ErraiEntityManager em,
          ErraiAttribute<X, Y> attr,
          X targetEntity,
          X sourceEntity) {
    ErraiIdentifiableType<Y> relatedEntityType = em.getMetamodel().entity(attr.getJavaType());
    Y oldRelatedEntity = attr.get(sourceEntity);
    Y resolvedEntity;
    if (oldRelatedEntity == null) {
      resolvedEntity = null;
    }
    else {
      Key<Y, ?> key = em.keyFor(oldRelatedEntity);
      resolvedEntity = em.find(key, Collections.<String,Object>emptyMap());
      if (resolvedEntity == null) {
        resolvedEntity = relatedEntityType.newInstance();
      }
    }
    attr.set(targetEntity, resolvedEntity);
  }

  private static <X, C, E> void copyPluralAssociation(
          ErraiEntityManager em,
          ErraiPluralAttribute<X, C, E> attr,
          X targetEntity,
          X sourceEntity) {
    C oldCollection = attr.get(sourceEntity);

    C newCollection;
    if (oldCollection == null) {
      newCollection = null;
    }
    else {
      newCollection = attr.createEmptyCollection();
      ErraiIdentifiableType<E> elemType = em.getMetamodel().entity(attr.getElementType().getJavaType());

      // TODO support map-valued plural attributes
      for (Object oldEntry : (Collection<?>) oldCollection) {
        Key<Object, ?> key = em.keyFor(oldEntry);
        Object resolvedEntry = em.find(key, Collections.<String,Object>emptyMap());
        if (resolvedEntry == null) {
          resolvedEntry = elemType.newInstance();
        }
        ((Collection) newCollection).add(resolvedEntry);
      }
    }
    attr.set(targetEntity, newCollection);
  }

  /**
   * Returns an inline JSON representation of the value of the given attribute
   * of the given entity instance.
   *
   * @param targetEntity
   *          The instance of the entity to retrieve the attribute value from.
   *          Not null.
   * @param attr
   *          The attribute to read from {@code targetEntity}. Not null.
   * @param eem
   *          The ErraiEntityManager that owns the entity. Not null.
   * @return a JSONValue that represents the requested attribute value of the
   *         given entity. Never null, although it could be JSONNull.
   */
  private <Y> JSONValue makeInlineJson(X targetEntity, ErraiAttribute<? super X, Y> attr, ErraiEntityManager eem) {
    Class<Y> attributeType = attr.getJavaType();
    Y attrValue = attr.get(Assert.notNull(targetEntity));

    // FIXME this should search all managed types, or maybe all embeddables. not just entities.
    // TODO it would be better to code-generate an Attribute.asJson() method than to do this at runtime
    if (eem.getMetamodel().getEntities().contains(attributeType)) {
      ErraiIdentifiableType<Y> attrEntityType = eem.getMetamodel().entity(attributeType);
      return attrEntityType.toJson(eem, attrValue);
    }

    return JsonUtil.basicValueToJson(attrValue);
  }

  protected <Y> void parseInlineJson(X targetEntity, ErraiAttribute<? super X, Y> attr, JSONValue attrJsonValue, ErraiEntityManager eem) {
    Class<Y> attributeType = attr.getJavaType();
    Y value;
    // FIXME this should search all managed types, or maybe all embeddables. not just entities.
    if (eem.getMetamodel().getEntities().contains(attributeType)) {
      ErraiIdentifiableType<Y> attrEntityType = eem.getMetamodel().entity(attributeType);
      value = attrEntityType.fromJson(eem, attrJsonValue);
    }
    else {
      value = JsonUtil.basicValueFromJson(attrJsonValue, attributeType);
    }

    attr.set(targetEntity, value);
  }

  /**
   * Returns a JSON object that represents a reference to the given attribute.
   * The reference is done by Entity identity (the type of the attribute is
   * assumed to be an entity type).
   *
   * @param targetEntity
   *          The instance of the entity to retrieve the attribute value from.
   *          Not null.
   * @param attr
   *          The attribute to read from {@code targetEntity}. Not null, and
   *          must be an entity type.
   * @param eem
   *          The ErraiEntityManager that owns the entity. Not null.
   * @return a JSONValue that is a reference to the given attribute value. Never
   *         null, although it could be JSONNull.
   */
  private <Y> JSONValue makeJsonReference(X targetEntity, ErraiSingularAttribute<? super X, Y> attr, ErraiEntityManager eem) {
    Class<Y> attributeType = attr.getJavaType();
    Y entityToReference = attr.get(targetEntity);
    if (entityToReference == null) {
      return JSONNull.getInstance();
    }
    ErraiIdentifiableType<Y> attrEntityType = eem.getMetamodel().entity(attributeType);
    if (attrEntityType == null) {
      throw new IllegalArgumentException("Can't make a reference to non-entity-typed attribute " + attr);
    }

    Object idToReference = attrEntityType.getId(Object.class).get(entityToReference);
    JSONValue ref;
    if (idToReference == null) {
      ref = JSONNull.getInstance();
    }
    else {
      // XXX attrEntityType is incorrect entityToReference is a subtype of attr.getJavaType()
      ref = new Key<Y, Object>(attrEntityType, idToReference).toJsonObject();
    }
    return ref;
  }

  /**
   * Returns a JSON object that represents a reference to the given attribute.
   * The reference is done by Entity identity (the type of the attribute is
   * assumed to be an entity type).
   *
   * @param targetEntity
   *          The instance of the entity to retrieve the attribute value from.
   *          Not null.
   * @param attr
   *          The attribute to read from {@code targetEntity}. Not null, and
   *          must be an entity type.
   * @param eem
   *          The ErraiEntityManager that owns the entity. Not null.
   * @return a JSONArray that contains references to each element in the given
   *         attribute's collection value. Returns JSONNull if the attribute has
   *         a null collection.
   */
  private <C, E> JSONValue makeJsonReference(X targetEntity, ErraiPluralAttribute<? super X, C, E> attr, ErraiEntityManager eem) {

    // XXX when we support maps, we should use getCollection()/getMap() and this will fix the type safety warnings
    C attrValue = attr.get(targetEntity);
    if (attrValue == null) {
      return JSONNull.getInstance();
    }

    Class<E> attributeType = attr.getElementType().getJavaType();
    ErraiIdentifiableType<E> attrEntityType = eem.getMetamodel().entity(attributeType);
    if (attrEntityType == null) {
      throw new IllegalArgumentException("Can't make a reference to collection of non-entity-typed attributes " + attr);
    }

    JSONArray array = new JSONArray();
    int index = 0;
    for (E element : (Iterable<E>) attrValue) {
      Object idToReference = attrEntityType.getId(Object.class).get(element);
      JSONValue ref;
      if (idToReference == null) {
        ref = JSONNull.getInstance();
      }
      else {
        // XXX attrEntityType is incorrect for collection elements that are subtypes of the attrEntityType
        ref = new Key<E, Object>(attrEntityType, idToReference).toJsonObject();
      }
      array.set(index++, ref);
    }
    return array;
  }

  protected <Y> void parseSingularJsonReference(
          X targetEntity, ErraiSingularAttribute<? super X, Y> attr, JSONValue attrJsonValue, ErraiEntityManager eem) {

    if (attrJsonValue == null || attrJsonValue.isNull() != null) return;

    Key<Y, ?> key = (Key<Y, ?>) Key.fromJsonObject(eem, attrJsonValue.isObject(), true);
    logger.trace("   looking for " + key);
    Y value = eem.find(key, Collections.<String,Object>emptyMap());
    attr.set(targetEntity, value);
  }

  protected <C, E> void parsePluralJsonReference(
          X targetEntity, ErraiPluralAttribute<? super X, C, E> attr, JSONArray attrJsonValues, ErraiEntityManager eem) {

    if (attrJsonValues == null || attrJsonValues.isNull() != null) return;

    Class<E> attributeElementType = attr.getElementType().getJavaType();
    ErraiIdentifiableType<E> attrEntityType = eem.getMetamodel().entity(attributeElementType);

    // FIXME this is broken for Map attributes
    // TODO when we support Map attributes, we should get the attribute with getCollection()/getMap() to fix this warning
    Collection<E> collection = (Collection<E>) attr.createEmptyCollection();

    for (int i = 0; i < attrJsonValues.size(); i++) {
      Key<E, ?> key = (Key<E, ?>) Key.fromJsonObject(eem, attrJsonValues.get(i).isObject(), true);

      logger.trace("   looking for " + key);
      E value = eem.getPartiallyConstructedEntity(key);
      if (value == null) {
        value = eem.find(key, Collections.<String,Object>emptyMap());
      }

      collection.add(value);
    }

    attr.set(targetEntity, (C) collection);
  }

  // ---------- JPA API Below This Line -------------

  @Override
  public Set<Attribute<? super X, ?>> getAttributes() {
    Set<Attribute<? super X, ?>> attributes = new HashSet<Attribute<? super X, ?>>();
    attributes.addAll(singularAttributes);
    attributes.addAll(pluralAttributes);
    return attributes;
  }

  @Override
  public Set<Attribute<X, ?>> getDeclaredAttributes() {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <Y> ErraiSingularAttribute<? super X, Y> getSingularAttribute(String name,
      Class<Y> type) {
    for (Attribute<? super X, ?> attr : singularAttributes) {
      if (attr.getName().equals(name)) {
        if (attr.getJavaType() != type) {
          throw new ClassCastException("Attribute \"" + name + "\" of entity " + getJavaType() +
                  " is not of the requested type " + type);
        }
        return (ErraiSingularAttribute<? super X, Y>) attr;
      }
    }
    return null;
  }

  @Override
  public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name,
      Class<Y> type) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
    return singularAttributes;
  }

  @Override
  public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public <E> CollectionAttribute<? super X, E> getCollection(String name,
      Class<E> elementType) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public <E> CollectionAttribute<X, E> getDeclaredCollection(String name,
      Class<E> elementType) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public <E> ListAttribute<? super X, E> getList(String name,
      Class<E> elementType) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public <E> ListAttribute<X, E> getDeclaredList(String name,
      Class<E> elementType) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public <K, V> MapAttribute<? super X, K, V> getMap(String name,
      Class<K> keyType, Class<V> valueType) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name,
      Class<K> keyType, Class<V> valueType) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
    return pluralAttributes;
  }

  @Override
  public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public ErraiAttribute<? super X, ?> getAttribute(String name) {
    // XXX would be better to keep attributes in a map
    for (Attribute<? super X, ?> attr : singularAttributes) {
      if (attr.getName().equals(name)) {
        return (ErraiAttribute<? super X, ?>) attr;
      }
    }
    for (Attribute<? super X, ?> attr : pluralAttributes) {
      if (attr.getName().equals(name)) {
        return (ErraiAttribute<? super X, ?>) attr;
      }
    }
    return null;
  }

  @Override
  public Attribute<X, ?> getDeclaredAttribute(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public CollectionAttribute<? super X, ?> getCollection(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public SetAttribute<? super X, ?> getSet(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public SetAttribute<X, ?> getDeclaredSet(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public ListAttribute<? super X, ?> getList(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public ListAttribute<X, ?> getDeclaredList(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public MapAttribute<? super X, ?, ?> getMap(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public Class<X> getJavaType() {
    return javaType;
  }

  @Override
  public String toString() {
    return "[ManagedType " + getJavaType().getName() + "]";
  }

}
