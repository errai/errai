package org.jboss.errai.reflections.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>A utility for matching simple package patterns to package names.
 *
 * <p>{@link #apply(String)} returns true iff an input is matched by at least one filter.
 *
 * @author mbarkley <mbarkley@redhat.com>
 */
public class SimplePackageFilter extends BasePackageFilter {

  /**
   * @param filters
   *          A collection of fully qualified class names or package patterns
   *          (package names with subpackage globbing i.e. org.jboss.errai.* or
   *          org.jboss.errai*).
   */
  public SimplePackageFilter(final Collection<String> filters) {
    super(filters);
  }

  @Override
  protected Collection<String> processFilters(final Collection<String> filters) {
    return new ArrayList<String>(filters);
  }

  @Override
  public boolean apply(final String input) {
    return matches(input);
  }

}
