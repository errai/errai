package org.jboss.errai.jpa.gen;

import java.lang.reflect.Member;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public class ErraiSingularAttribute<X, T> implements SingularAttribute<X, T> {

  private final String name;
  private final javax.persistence.metamodel.Attribute.PersistentAttributeType persistentAttributeType;
  private final ManagedType<X> declaringType;
  private final Class<T> javaType;
  private final Member javaMember;
  private final boolean isAssociation;
  private final boolean isCollection;
  private final javax.persistence.metamodel.Bindable.BindableType bindableType;
  private final Class<T> bindableJavaType;
  private final boolean isId;
  private final boolean isVersion;
  private final boolean isOptional;
  private final Type<T> type;

  ErraiSingularAttribute(
      String name,
      PersistentAttributeType persistentAttributeType,
      ManagedType<X> declaringType,
      Class<T> javaType,
      Member javaMember,
      boolean isAssociation,
      boolean isCollection,
      BindableType bindableType,
      Class<T> bindableJavaType,
      boolean isId,
      boolean isVersion,
      boolean isOptional,
      Type<T> type) {
        this.name = name;
        this.persistentAttributeType = persistentAttributeType;
        this.declaringType = declaringType;
        this.javaType = javaType;
        this.javaMember = javaMember;
        this.isAssociation = isAssociation;
        this.isCollection = isCollection;
        this.bindableType = bindableType;
        this.bindableJavaType = bindableJavaType;
        this.isId = isId;
        this.isVersion = isVersion;
        this.isOptional = isOptional;
        this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public javax.persistence.metamodel.Attribute.PersistentAttributeType getPersistentAttributeType() {
    return persistentAttributeType;
  }

  @Override
  public ManagedType<X> getDeclaringType() {
    return declaringType;
  }

  @Override
  public Class<T> getJavaType() {
    return javaType;
  }

  @Override
  public Member getJavaMember() {
    return javaMember;
  }

  @Override
  public boolean isAssociation() {
    return isAssociation;
  }

  @Override
  public boolean isCollection() {
    return isCollection;
  }

  @Override
  public javax.persistence.metamodel.Bindable.BindableType getBindableType() {
    return bindableType;
  }

  @Override
  public Class<T> getBindableJavaType() {
    return bindableJavaType;
  }

  @Override
  public boolean isId() {
    return isId;
  }

  @Override
  public boolean isVersion() {
    return isVersion;
  }

  @Override
  public boolean isOptional() {
    return isOptional;
  }

  @Override
  public Type<T> getType() {
    return type;
  }

}
