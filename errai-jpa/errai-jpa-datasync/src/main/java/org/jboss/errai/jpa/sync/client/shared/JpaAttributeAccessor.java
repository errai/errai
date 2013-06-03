package org.jboss.errai.jpa.sync.client.shared;

import javax.persistence.metamodel.Attribute;

/**
 * JPA attribute values cannot be obtained the same way on the client as on the
 * server. This interface is the neutral ground that can be used in shared code.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public interface JpaAttributeAccessor {

  /**
   * Retrieves the current value of the given attribute from the given entity.
   * @param attribute
   *          The attribute to retrieve. Must not be null.
   * @param entity
   *          the entity instance to read the attribute value from. Must not be null.
   *
   * @return The value of the given attribute. May be null.
   * @throws NullPointerException if either argument is null.
   */
  <X, Y> Y get(Attribute<X, Y> attribute, X entity);

  /**
   * Sets the current value of the given attribute on the given entity.
   *
   * @param entity
   *          the entity instance to read the attribute value from. Must not be
   *          null.
   * @param attribute
   *          The attribute to retrieve. Must not be null.
   * @param value
   *          The new value for the attribute. May be null.
   * @throws NullPointerException
   *           if <code>attribute</code> or <code>entity</code> is null.
   */
  <X, Y> void set(Attribute<X, Y> attribute, X entity, Y value);

}
