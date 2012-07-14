package org.jboss.errai.ioc.rebind.ioc.graph;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;

/**
 * @author Mike Brock
 */
public class Dependency {
  private final MetaClass type;

  private Dependency(final MetaClass type) {
    this.type = type;
  }

  public static Dependency on(final MetaClass type) {
    return new Dependency(type);
  }

  public static Dependency on(final Class<?> type) {
    return new Dependency(MetaClassFactory.get(type));
  }

  public MetaClass getType() {
    return type;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Dependency)) return false;

    final Dependency that = (Dependency) o;

    if (type != null ? !type.equals(that.type) : that.type != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return type != null ? type.hashCode() : 0;
  }
}
