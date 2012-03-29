package org.jboss.errai.ioc.rebind.ioc.graph;

import org.jboss.errai.codegen.meta.MetaClass;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class ProxySortUnit extends SortUnit {
  private SortUnit delegate;

  private ProxySortUnit(MetaClass type, boolean hard) {
    super(type, Collections.<Object>emptyList(), Collections.<SortUnit>emptySet(), hard);
  }

  public static ProxySortUnit proxyOf(MetaClass type) {
    return new ProxySortUnit(type, false);
  }

  public static ProxySortUnit hardDepProxy(final MetaClass type,final SortUnit unit) {
    return new ProxySortUnit(type, true) {

      @Override
      public Set<SortUnit> getDependencies() {
        return unit.getDependencies();
      }

      @Override
      public SortUnit getDependency(SortUnit unit) {
        return unit.getDependency(unit);
      }

      @Override
      public List<Object> getItems() {
        return unit.getItems();
      }

      @Override
      public MetaClass getType() {
        return unit.getType();
      }

      @Override
      public int getDepth() {
        return unit.getDepth();
      }

      @Override
      public int compareTo(SortUnit o) {
        return unit.compareTo(o);
      }

      @Override
      public String toString() {
        return unit.toString();
      }
    };
  }

  @Override
  public Set<SortUnit> getDependencies() {
    return delegate.getDependencies();
  }

  @Override
  public SortUnit getDependency(SortUnit unit) {
    return delegate.getDependency(unit);
  }

  @Override
  public List<Object> getItems() {
    return delegate.getItems();
  }

  @Override
  public MetaClass getType() {
    return delegate.getType();
  }

  @Override
  public int getDepth() {
    return delegate.getDepth();
  }

  @Override
  public int compareTo(SortUnit o) {
    return delegate.compareTo(o);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  public void setDelegate(SortUnit unit) {
    this.delegate = unit;
  }

}
