package org.jboss.errai.jpa.client.local;

import java.util.Collection;
import java.util.HashSet;
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
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.jboss.errai.common.client.framework.Assert;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public abstract class ErraiEntityType<X> implements EntityType<X> {

  private final Set<SingularAttribute<? super X, ?>> singularAttributes = new HashSet<SingularAttribute<? super X,?>>();
  private final Set<PluralAttribute<? super X, ?, ?>> pluralAttributes = new HashSet<PluralAttribute<? super X, ?, ?>>();

  private SingularAttribute<? super X, ?> id;
  private SingularAttribute<? super X, ?> version;

  private final String name;
  private final Class<X> javaType;

  public ErraiEntityType(String name, Class<X> javaType) {
    this.name = name;
    this.javaType = javaType;
  }

  public <Y> void addAttribute(Attribute<X, Y> attribute) {
    if (attribute instanceof SingularAttribute) {
      SingularAttribute<? super X, ?> sa = (SingularAttribute<? super X, ?>) attribute;
      singularAttributes.add(sa);
      if (sa.isId()) id = sa;
      if (sa.isVersion()) version = sa;
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
   * Creates and returns a new instance of the represented entity type.
   *
   * @return a new instance of type X.
   */
  public abstract X newInstance();

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

  public JSONValue toJson(EntityManager em, X targetEntity) {
    final ErraiEntityManager eem = (ErraiEntityManager) em;
    JSONObject jsonValue = new JSONObject();

    // TODO get all attributes, not just singular ones
    for (Attribute<? super X, ?> a : getAttributes()) {
      ErraiAttribute<? super X, ?> attr = (ErraiAttribute<? super X, ?>) a;
      switch (attr.getPersistentAttributeType()) {
      case ELEMENT_COLLECTION:
      case EMBEDDED:
      case BASIC:
        jsonValue.put(attr.getName(), makeInlineJson(targetEntity, attr, eem));
      break;

      case MANY_TO_MANY:
      case MANY_TO_ONE:
      case ONE_TO_MANY:
      case ONE_TO_ONE:
        JSONValue attributeValue;
        if (attr instanceof ErraiSingularAttribute) {
          attributeValue = makeJsonReference(targetEntity, (ErraiSingularAttribute<? super X, ?>) attr, eem);
        }
        else if (attr instanceof ErraiPluralAttribute) {
          attributeValue = makeJsonReference(targetEntity, (ErraiPluralAttribute<? super X, ?, ?>) attr, eem);
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
      ErraiEntityType<Y> attrEntityType = eem.getMetamodel().entity(attributeType);
      return attrEntityType.toJson(eem, attrValue);
    }

    return JsonUtil.basicValueToJson(attrValue);
  }

  private <Y> void parseInlineJson(X targetEntity, ErraiAttribute<? super X, Y> attr, JSONValue attrJsonValue, ErraiEntityManager eem) {
    Class<Y> attributeType = attr.getJavaType();
    Y value;
    // FIXME this should search all managed types, or maybe all embeddables. not just entities.
    if (eem.getMetamodel().getEntities().contains(attributeType)) {
      ErraiEntityType<Y> attrEntityType = eem.getMetamodel().entity(attributeType);
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
    Y attrValue = attr.get(targetEntity);
    if (attrValue == null) {
      return JSONNull.getInstance();
    }
    ErraiEntityType<Y> attrEntityType = eem.getMetamodel().entity(attributeType);
    if (attrEntityType == null) {
      throw new IllegalArgumentException("Can't make a reference to non-entity-typed attribute " + attr);
    }

    JSONObject ref = new JSONObject();
    ref.put("entityReference", attrEntityType.makeInlineJson(attrValue, attrEntityType.getId(Object.class), eem));
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
    ErraiEntityType<E> attrEntityType = eem.getMetamodel().entity(attributeType);
    if (attrEntityType == null) {
      throw new IllegalArgumentException("Can't make a reference to collection of non-entity-typed attributes " + attr);
    }

    JSONArray array = new JSONArray();
    int index = 0;
    for (E element : (Iterable<E>) attrValue) {
      Object idToReference = attrEntityType.getId(Object.class).get(element);
      JSONObject ref = new JSONObject();
      ref.put("entityReference", JsonUtil.basicValueToJson(idToReference));
      array.set(index++, ref);
    }
    return array;
  }

  private <Y> void parseSingularJsonReference(
          X targetEntity, ErraiSingularAttribute<? super X, Y> attr, JSONValue attrJsonValue, ErraiEntityManager eem) {

    if (attrJsonValue == null || attrJsonValue.isNull() != null) return;

    Class<Y> attributeType = attr.getJavaType();
    ErraiEntityType<Y> attrEntityType = eem.getMetamodel().entity(attributeType);

    JSONValue idJson = attrJsonValue.isObject().get("entityReference");
    Class<?> idType = attrEntityType.getId(Object.class).getJavaType();
    Object id = JsonUtil.basicValueFromJson(idJson, idType);

    System.out.println("   looking for " + attrEntityType.getJavaType() + " with id " + id);
    Y value = eem.find(attrEntityType.getJavaType(), id);
    attr.set(targetEntity, value);
  }

  private <C, E> void parsePluralJsonReference(
          X targetEntity, ErraiPluralAttribute<? super X, C, E> attr, JSONArray attrJsonValues, ErraiEntityManager eem) {

    if (attrJsonValues == null || attrJsonValues.isNull() != null) return;

    Class<E> attributeElementType = attr.getElementType().getJavaType();
    ErraiEntityType<E> attrEntityType = eem.getMetamodel().entity(attributeElementType);

    // FIXME this is broken for Map attributes
    // TODO when we support Map attributes, we should get the attribute with getCollection()/getMap() to fix this warning
    Collection<E> collection = (Collection<E>) attr.createEmptyCollection();

    for (int i = 0; i < attrJsonValues.size(); i++) {
      JSONValue idJson = attrJsonValues.get(i).isObject().get("entityReference");
      Class<?> idType = attrEntityType.getId(Object.class).getJavaType();
      Object id = JsonUtil.basicValueFromJson(idJson, idType);

      System.out.println("   looking for " + attrEntityType.getJavaType() + " with id " + id);
      E value = eem.getPartiallyConstructedEntity(Key.get(eem, attrEntityType.getJavaType(), id));
      if (value == null) {
        value = eem.find(attrEntityType.getJavaType(), id);
      }

      collection.add(value);
    }

    attr.set(targetEntity, (C) collection);
  }

  private Key<X, ?> keyFromJson(JSONValue json) {
    JSONValue keyJson = json.isObject().get(id.getName());
    Object idValue = JsonUtil.basicValueFromJson(keyJson, id.getJavaType());
    return new Key<X, Object>(this, idValue);
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

  @Override
  public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name,
      Class<Y> type) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
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
  public Attribute<? super X, ?> getAttribute(String name) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
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
  public javax.persistence.metamodel.Type.PersistenceType getPersistenceType() {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
  }

  @Override
  public Class<X> getJavaType() {
    return javaType;
  }

  @Override
  public javax.persistence.metamodel.Bindable.BindableType getBindableType() {
    return BindableType.ENTITY_TYPE;
  }

  @Override
  public Class<X> getBindableJavaType() {
    return javaType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "[EntityType \"" + getName() + "\" (" + getJavaType().getName() + ")]";
  }
}
