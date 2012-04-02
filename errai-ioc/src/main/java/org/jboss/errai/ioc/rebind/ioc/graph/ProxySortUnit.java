package org.jboss.errai.ioc.rebind.ioc.graph;

import org.jboss.errai.codegen.meta.MetaClass;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A cycle breaking proxy used by the {@link GraphBuilder} to properly construct a graph which can accurately
 * represent cycles. A <tt>ProxySortUnit</tt> must be closed by calling {@link #setDelegate(SortUnit)} before
 * being put into use, otherwise calls to any methods will result in {@link NullPointerException}.
 *
 * @author Mike Brock
 */
public class ProxySortUnit extends SortUnit {
  private SortUnit delegate;

  private ProxySortUnit(MetaClass type) {
    super(type, Collections.<Object>emptyList(), Collections.<SortUnit>emptySet());
  }

  /**
   * Creates a new proxied SortUnit on the specified type.
   *
   * @param type the sort unit
   * @return a new instance of ProxySortUnit
   */
  public static ProxySortUnit proxyOf(MetaClass type) {
    return new ProxySortUnit(type);
  }

  @Override
  public Set<SortUnit> getDependencies() {
    return delegate.getDependencies();
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

  /**
   * Sets the instance of {@link SortUnit} which should be proxied on.
   * @param unit
   */
  public void setDelegate(SortUnit unit) {
    this.delegate = unit;
  }

}
