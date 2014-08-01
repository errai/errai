package org.jboss.errai.reflections.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>A utility for matching package patterns to file paths.
 *
 * <p>{@link #apply(String)} returns false iff an input is matched by at least one filter.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class SimplePackageFilePathPredicate extends BasePackageFilter {

  public SimplePackageFilePathPredicate(final Collection<String> filters) {
    super(filters);
  }

  @Override
  protected Collection<String> processFilters(Collection<String> filters) {
    final Collection<String> processedFilters = new ArrayList<String>(filters.size());

    for (final String filter : filters) {
      String filterPath = filter.replace('.', File.separatorChar);
      if (!filterPath.endsWith("*"))
        filterPath += ".class";
      processedFilters.add(filterPath);
    }

    return processedFilters;
  }

  @Override
  public boolean apply(String input) {
    return !matches(input);
  }
}
