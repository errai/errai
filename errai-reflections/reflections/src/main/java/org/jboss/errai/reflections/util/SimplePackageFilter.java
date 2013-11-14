package org.jboss.errai.reflections.util;

import java.util.Collection;

/**
 * A utility for matching simple package patterns to package names.
 * 
 * @author mbarkley <mbarkley@redhat.com>
 */
public class SimplePackageFilter {

  private final Collection<String> filters;

  /**
   * @param filters
   *          A collection of fully qualified class names or package patterns
   *          (package names with subpackage globbing i.e. org.jboss.errai.* or
   *          org.jboss.errai*).
   */
  public SimplePackageFilter(Collection<String> filters) {
    this.filters = filters;
  }

  /**
   * @param className
   *          The class name to be matched.
   * @return True iff the class name matches a package filter.
   */
  public boolean matches(final String className) {
    for (final String pattern : filters) {
      boolean res;
      if (pattern.endsWith("*")) {
        res = className.startsWith(pattern.substring(0, pattern.length()-1));
      }
      else {
        res = className.equals(pattern);
      }
      if (res) {
        return true;
      }
    }

    return false;
  }

}
