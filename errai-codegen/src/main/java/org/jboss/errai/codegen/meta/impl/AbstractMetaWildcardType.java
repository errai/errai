package org.jboss.errai.codegen.meta.impl;

import org.jboss.errai.codegen.meta.MetaWildcardType;

/**
 * Base implementation for implementations of {@link MetaWildcardType},
 * providing uniform hashCode, equals, and toString implementations.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public abstract class AbstractMetaWildcardType implements MetaWildcardType {

  @Override
  public final String toString() {
    return getName();
  }

  @Override
  public final boolean equals(Object other) {
    return other instanceof AbstractMetaWildcardType &&
            getName().equals(((AbstractMetaWildcardType) other).getName());
  }

  @Override
  public final int hashCode() {
    return getName().hashCode();
  }
}
