package org.jboss.errai.bus.server.websocket.jsr356.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Lookup of defined {@link WebSocketFilter}.
 * 
 * @author Michel Werren
 */
public class FilterLookup {

  private static final Logger LOGGER = LoggerFactory.getLogger(FilterLookup.class.getName());

  private static final FilterLookup INSTANCE = new FilterLookup();

  /**
   * Reference of an alternative to delegate the filter lookup.
   */
  private static FilterLookup delegate = null;

  protected List<WebSocketFilter> filters = null;

  protected FilterLookup() {
  }

  public static FilterLookup getInstance() {
    return delegate != null ? delegate : INSTANCE;
  }

  public static void registerDelegate(FilterLookup delegate) {
    FilterLookup.delegate = delegate;
  }

  /**
   * get classes of filters and create instances of them.
   * 
   * @param filterClassNames
   */
  public void initFilters(StringTokenizer filterClassNames) {
    filters = new ArrayList<WebSocketFilter>(filterClassNames.countTokens());
    final List<Class> filterClasses = getFilterClasses(filterClassNames);
    for (Class filterClass : filterClasses) {
      final Object filterInstance;
      try {
        filterInstance = filterClass.newInstance();
        filters.add((WebSocketFilter) filterInstance);
      } catch (InstantiationException e) {
        printFilterLookupError(e, filterClass.getName(), LOGGER);
      } catch (IllegalAccessException e) {
        printFilterLookupError(e, filterClass.getName(), LOGGER);
      }
    }
    for (WebSocketFilter filter : filters) {
      LOGGER.info("found Errai websocket filter: {}", filter.getClass().getName());
    }
  }

  /**
   * @param filterClassNames
   * @return classes of all filters
   */
  protected List<Class> getFilterClasses(StringTokenizer filterClassNames) {
    List<Class> out = new ArrayList<Class>(filterClassNames.countTokens());
    while (filterClassNames.hasMoreTokens()) {
      final String filterClassName = filterClassNames.nextToken().trim();
      if (!filterClassName.isEmpty()) {
        try {
          out.add(Class.forName(filterClassName));
        } catch (ClassNotFoundException e) {
          printFilterLookupError(e, filterClassName, LOGGER);
        }
      }
    }
    return out;
  }

  public List<WebSocketFilter> getFilters() {
    return filters;
  }

  protected void printFilterLookupError(Throwable throwable, String filterClassName, Logger logger) {
    logger.error("could not lookup Errai Websocket filter: " + filterClassName, throwable);
    throw new RuntimeException(throwable);
  }
}
