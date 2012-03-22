package org.jboss.errai.jpa.gen;

import java.util.Set;

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

public class ErraiEntityType<X> implements EntityType<X> {

  private final SingularAttribute<? super X, ?> id;
  private final SingularAttribute<? super X, ?> version;
  private final IdentifiableType<? super X> supertype;

  public ErraiEntityType(SingularAttribute<? super X, ?> id,
      SingularAttribute<? super X, ?> version,
      IdentifiableType<? super X> supertype) {
    this.id = id;
    this.version = version;
    this.supertype = supertype;
  }

  @Override
  public <Y> SingularAttribute<? super X, Y> getId(Class<Y> type) {
    return (SingularAttribute<? super X, Y>) id;
  }

  @Override
  public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
    // XXX the JPA spec is not clear on the difference between id and declaredId
    return (SingularAttribute<X, Y>) id;
  }

  @Override
  public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type) {
    return (SingularAttribute<? super X, Y>) version;
  }

  @Override
  public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type) {
    // XXX the JPA spec is not clear on the difference between version and declaredVersion
    return (SingularAttribute<X, Y>) version;
  }

  @Override
  public IdentifiableType<? super X> getSupertype() {
    return supertype;
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Attribute<X, ?>> getDeclaredAttributes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name,
      Class<Y> type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name,
      Class<Y> type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <E> CollectionAttribute<? super X, E> getCollection(String name,
      Class<E> elementType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <E> CollectionAttribute<X, E> getDeclaredCollection(String name,
      Class<E> elementType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <E> ListAttribute<? super X, E> getList(String name,
      Class<E> elementType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <E> ListAttribute<X, E> getDeclaredList(String name,
      Class<E> elementType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <K, V> MapAttribute<? super X, K, V> getMap(String name,
      Class<K> keyType, Class<V> valueType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name,
      Class<K> keyType, Class<V> valueType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Attribute<? super X, ?> getAttribute(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Attribute<X, ?> getDeclaredAttribute(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CollectionAttribute<? super X, ?> getCollection(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SetAttribute<? super X, ?> getSet(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SetAttribute<X, ?> getDeclaredSet(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ListAttribute<? super X, ?> getList(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ListAttribute<X, ?> getDeclaredList(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MapAttribute<? super X, ?, ?> getMap(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public javax.persistence.metamodel.Type.PersistenceType getPersistenceType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<X> getJavaType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public javax.persistence.metamodel.Bindable.BindableType getBindableType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<X> getBindableJavaType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }


}
