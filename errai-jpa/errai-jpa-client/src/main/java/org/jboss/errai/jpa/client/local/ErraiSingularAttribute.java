package org.jboss.errai.jpa.client.local;

import javax.persistence.metamodel.SingularAttribute;

/**
 * Extends the JPA SingularAttribute interface with methods required by Errai
 * persistence but missing from the JPA metamodel. Most importantly, this
 * interface provides methods for reading and writing the attribute value.
 *
 * @param <X>
 *          The type containing the represented attribute
 * @param <T>
 *          The type of the represented attribute
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface ErraiSingularAttribute<X, T> extends SingularAttribute<X, T> {

  /**
   * Retrieves the value of this attribute from the given entity instance.
   *
   * @param entityInstance
   *          The entity to retrieve the entity value from. The type of this
   *          argument must be assignable to the declaring entity's type
   *          (returned by {@link #getDeclaringType()}).
   * @return The value of this attribute on the given entity instance.
   */
  public T get(X entityInstance);

  /**
   * Sets the value of this attribute on the given entity instance.
   *
   * @param entityInstance
   *          The entity to set this attribute value on. The type of this
   *          argument must be assignable to the declaring entity's type
   *          (returned by {@link #getDeclaringType()}).
   * @param value
   *          The value to set the attribute to.
   */
  public void set(X entityInstance, T value);

  /**
   * Can the attribute's value be generated (usually for ID attributes).
   */
  public boolean isGeneratedValue();
}
