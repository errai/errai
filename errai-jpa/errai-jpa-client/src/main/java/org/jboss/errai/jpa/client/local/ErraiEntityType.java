package org.jboss.errai.jpa.client.local;

import javax.persistence.metamodel.EntityType;

/**
 * Errai implementation of the JPA EntityType metamodel interface. Specializes
 * IdentifiableType by adding {@code name} and {@code bindableType} properties.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 *
 * @param <X> The actual entity type described by this metatype.
 */
public abstract class ErraiEntityType<X> extends ErraiIdentifiableType<X> implements EntityType<X> {

  private final String name;

  public ErraiEntityType(String name, Class<X> javaType) {
    super(javaType);
    this.name = name;
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
  public javax.persistence.metamodel.Bindable.BindableType getBindableType() {
    return BindableType.ENTITY_TYPE;
  }

  @Override
  public String toString() {
    return "[EntityType \"" + getName() + "\" (" + getJavaType().getName() + ")]";
  }
}
