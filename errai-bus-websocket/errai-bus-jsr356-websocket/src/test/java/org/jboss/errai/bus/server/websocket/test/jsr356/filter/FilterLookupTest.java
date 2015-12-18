package org.jboss.errai.bus.server.websocket.test.jsr356.filter;

import org.jboss.errai.bus.server.websocket.jsr356.filter.FilterLookup;
import org.jboss.errai.bus.server.websocket.jsr356.filter.WebSocketFilter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.StringTokenizer;

public class FilterLookupTest {

  private static final String FILTER_NAMES = "org.jboss.errai.bus.server.websocket.test.jsr356.filter.FooFilter, org.jboss.errai.bus.server.websocket.test.jsr356.filter.BarFilter";
  private static final String FAIL_NAMES = "or.jboss.errai.bus.server.websocket.test.jsr356.filter.FooFilter, org.jboss.errai.bus.server.websocket.test.jsr356.filter.BarFilte";

  @Test(expected = RuntimeException.class)
  public void testInitFilters() throws Exception {
    final StringTokenizer filterTokenizer = new StringTokenizer(FILTER_NAMES,
            ",");
    final FilterLookup filterLookup = FilterLookup.getInstance();

    filterLookup.initFilters(filterTokenizer);

    final List<WebSocketFilter> filters = filterLookup.getFilters();
    boolean fooFilterFound = false;
    boolean barFilterFound = false;
    for (WebSocketFilter filter : filters) {
      fooFilterFound = filter.getClass().equals(FooFilter.class)
              || fooFilterFound;
      barFilterFound = filter.getClass().equals(BarFilter.class)
              || barFilterFound;
    }

    Assert.assertTrue("filter not found", fooFilterFound && barFilterFound);

    final StringTokenizer failTokenizer = new StringTokenizer(FAIL_NAMES, ",");
    filterLookup.initFilters(failTokenizer);
  }
}
