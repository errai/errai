package org.jboss.errai.ioc.rebind.ioc.graph;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;

/**
 * @author Mike Brock
 */
public class Dependency {
  private final MetaClass type;
  private final boolean hard;

  private Dependency(MetaClass type, boolean hard) {
    this.type = type;
    this.hard = hard;
  }

  public static Dependency on(MetaClass type) {
    return new Dependency(type, false);
  }


  public static Dependency hard(MetaClass type) {
    return new Dependency(type, true);
  }

  public static Dependency on(final Class<?> type) {
    return new Dependency(MetaClassFactory.get(type), false);
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

  public boolean isHard() {
    return hard;
  }
}
