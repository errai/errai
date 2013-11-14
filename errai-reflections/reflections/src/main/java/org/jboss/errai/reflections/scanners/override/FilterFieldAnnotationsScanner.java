package org.jboss.errai.reflections.scanners.override;

import org.jboss.errai.reflections.scanners.FieldAnnotationsScanner;
import org.jboss.errai.reflections.util.SimplePackageFilter;

/**
 * A {@link FieldAnnotationsScanner} that does not scan classes matching a given package filter.
 * 
 * @author mbarkley <mbarkley@redhat.com>
 */
public class FilterFieldAnnotationsScanner extends FieldAnnotationsScanner {

  private final SimplePackageFilter filter;
  
  public FilterFieldAnnotationsScanner(SimplePackageFilter filter) {
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
    return FieldAnnotationsScanner.class.getName();
  }
}
