package org.jboss.errai.jpa.client.local;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
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

  public X fromJson(EntityManager em, JSONValue jsonValue) {
    final ErraiEntityManager eem = (ErraiEntityManager) em;
    X entity = newInstance();
    // TODO get all attributes, not just singular ones
    for (SingularAttribute<? super X, ?> a : getSingularAttributes()) {
      ErraiSingularAttribute<? super X, ?> attr = (ErraiSingularAttribute<? super X, ?>) a;
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
        // TODO parseJsonReference(entity, attr, attrJsonValue, eem);
      }
    }
    return entity;
  }

  public JSONValue toJson(EntityManager em, X targetEntity) {
    final ErraiEntityManager eem = (ErraiEntityManager) em;
    JSONObject jsonValue = new JSONObject();

    // TODO get all attributes, not just singular ones
    for (SingularAttribute<? super X, ?> a : getSingularAttributes()) {
      ErraiSingularAttribute<? super X, ?> attr = (ErraiSingularAttribute<? super X, ?>) a;
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
        jsonValue.put(attr.getName(), makeJsonReference(targetEntity, attr, eem));
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
  private <Y> JSONValue makeInlineJson(X targetEntity, ErraiSingularAttribute<? super X, Y> attr, ErraiEntityManager eem) {
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

  @SuppressWarnings("unchecked")
  private <Y> void parseInlineJson(X targetEntity, ErraiSingularAttribute<? super X, Y> attr, JSONValue attrJsonValue, ErraiEntityManager eem) {
    Class<Y> attributeType = attr.getJavaType();
    Y value;
    if (attrJsonValue.isNull() != null) {
      value = null;
    }
    // FIXME this should search all managed types, or maybe all embeddables. not just entities.
    else if (eem.getMetamodel().getEntities().contains(attributeType)) {
      ErraiEntityType<Y> attrEntityType = eem.getMetamodel().entity(attributeType);
      value = attrEntityType.fromJson(eem, attrJsonValue);
    }
    else if (attributeType == String.class) {
      value = (Y) attrJsonValue.isString().stringValue();
    }
    else if (attributeType == boolean.class || attributeType == Boolean.class) {
      value = (Y) Boolean.valueOf(attrJsonValue.isBoolean().booleanValue());
    }
    else if (attributeType == BigInteger.class) {
      value = (Y) new BigInteger(attrJsonValue.isString().stringValue());
    }
    else if (attributeType == BigDecimal.class) {
      value = (Y) new BigDecimal(attrJsonValue.isString().stringValue());
    }
    else if (attributeType == byte.class || attributeType == Byte.class) {
      value = (Y) Byte.valueOf((byte) attrJsonValue.isNumber().doubleValue());
    }
    else if (attributeType == char.class || attributeType == Character.class) {
      value = (Y) Character.valueOf(attrJsonValue.isString().stringValue().charAt(0));
    }
    else if (attributeType == short.class || attributeType == Short.class) {
      value = (Y) Short.valueOf((short) attrJsonValue.isNumber().doubleValue());
    }
    else if (attributeType == int.class || attributeType == Integer.class) {
      value = (Y) Integer.valueOf((int) attrJsonValue.isNumber().doubleValue());
    }
    else if (attributeType == long.class || attributeType == Long.class) {
      value = (Y) Long.valueOf(attrJsonValue.isString().stringValue());
    }
    else if (attributeType == float.class || attributeType == Float.class) {
      value = (Y) Float.valueOf((float) attrJsonValue.isNumber().doubleValue());
    }
    else if (attributeType == double.class || attributeType == Double.class) {
      value = (Y) Double.valueOf(attrJsonValue.isNumber().doubleValue());
    }
    else if (attributeType == Date.class) {
      value = (Y) new Date(Long.parseLong(attrJsonValue.isString().stringValue()));
    }
    else if (attributeType == java.sql.Date.class) {
      value = (Y) new java.sql.Date(Long.parseLong(attrJsonValue.isString().stringValue()));
    }
    else if (attributeType == Time.class) {
      value = (Y) new Time(Long.parseLong(attrJsonValue.isString().stringValue()));
    }
    else if (attributeType == Timestamp.class) {
      value = (Y) Timestamp.valueOf(attrJsonValue.isString().stringValue());
    }
    else if (attributeType.isEnum()) {
      @SuppressWarnings("rawtypes") Class enumType = attributeType;
      value = (Y) Enum.valueOf(enumType, attrJsonValue.isString().stringValue());
    }
    else if (attributeType == byte[].class) {
      value = (Y) Base64Util.decode(attrJsonValue.isString().stringValue());
    }
    else if (attributeType == Byte[].class) {
      value = (Y) Base64Util.decodeAsBoxed(attrJsonValue.isString().stringValue());
    }
    else if (attributeType == char[].class) {
      value = (Y) attrJsonValue.isString().stringValue().toCharArray();
    }
    else if (attributeType == Character[].class) {
      String str = attrJsonValue.isString().stringValue();
      Character[] boxedArray = new Character[str.length()];
      for (int i = 0; i < str.length(); i++) {
        boxedArray[i] = str.charAt(i);
      }
      value = (Y) boxedArray;
    }
    else {
      throw new RuntimeException("I don't know how unJSONify attribute " + attr);
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
    throw new RuntimeException("Not implemented");
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
