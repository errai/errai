package org.jboss.errai.reflections.scanners.override;

import org.jboss.errai.reflections.scanners.TypeAnnotationsScanner;
import org.jboss.errai.reflections.util.SimplePackageFilter;

/**
 * A {@link TypeAnnotationsScanner} that does not scan classes matching a given package filter.
 * 
 * @author mbarkley <mbarkley@redhat.com>
 */
public class FilterTypeAnnotationsScanner extends TypeAnnotationsScanner {
  
  private final SimplePackageFilter filter;
  
  public FilterTypeAnnotationsScanner(SimplePackageFilter filter) {
    this.filter = filter;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void scan(Object cls) {
    if (!filter.matches(getMetadataAdapter().getClassName(cls))) {
      super.scan(cls);
    }
  }
  
  @Override
  public String getName() {
    return TypeAnnotationsScanner.class.getName();
  }
}
