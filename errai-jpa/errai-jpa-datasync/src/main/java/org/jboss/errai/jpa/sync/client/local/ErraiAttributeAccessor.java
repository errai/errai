package org.jboss.errai.jpa.sync.client.local;

import javax.persistence.metamodel.Attribute;

import org.jboss.errai.jpa.client.local.ErraiAttribute;
import org.jboss.errai.jpa.sync.client.shared.JpaAttributeAccessor;

/**
 * Implementation of {@link JpaAttributeAccessor} that works with Errai's
 * generated client-side Attribute objects (which have their own get() and set()
 * methods).
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class ErraiAttributeAccessor implements JpaAttributeAccessor {

  @Override
  public <X, Y> Y get(Attribute<X, Y> attribute, X entity) {
    return ((ErraiAttribute<X, Y>) attribute).get(entity);
  }

  @Override
  public <X, Y> void set(Attribute<X, Y> attribute, X entity, Y value) {
    ((ErraiAttribute<X, Y>) attribute).set(entity, value);
  }

}
