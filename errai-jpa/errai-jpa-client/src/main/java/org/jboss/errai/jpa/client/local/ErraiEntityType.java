package org.jboss.errai.jpa.client.local;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.PrePersist;
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

public abstract class ErraiEntityType<X> implements EntityType<X> {

  private final Set<SingularAttribute<? super X, ?>> singularAttributes = new HashSet<SingularAttribute<? super X,?>>();

  private SingularAttribute<? super X, ?> id;
  private SingularAttribute<? super X, ?> version;

  private final String name;
  private final Class<X> javaType;

  public ErraiEntityType(String name, Class<X> javaType) {
    this.name = name;
    this.javaType = javaType;
  }

  public <Y> void addAttribute(SingularAttribute<X, Y> attribute) {
    singularAttributes.add(attribute);
    if (attribute.isId()) id = attribute;
    if (attribute.isVersion()) version = attribute;
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
  public abstract void deliverPostLoad(X targetEntity);

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
    // TODO Auto-generated method stub
    throw new RuntimeException("Not implemented");
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
