package org.jboss.errai.ioc.rebind.ioc.graph;

import org.jboss.errai.codegen.meta.MetaClass;

/**
 * @author Mike Brock
 */
public class Dependency {
  private final MetaClass type;

  private Dependency(MetaClass type) {
    this.type = type;
  }

  public static Dependency on(MetaClass type) {
    return new Dependency(type);
  }

  public MetaClass getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Dependency)) return false;

    Dependency that = (Dependency) o;

    if (type != null ? !type.equals(that.type) : that.type != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return type != null ? type.hashCode() : 0;
  }
}
