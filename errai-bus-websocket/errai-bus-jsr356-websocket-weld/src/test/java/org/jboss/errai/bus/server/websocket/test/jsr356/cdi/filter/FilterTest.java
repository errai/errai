package org.jboss.errai.bus.server.websocket.test.jsr356.cdi.filter;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.errai.bus.server.websocket.jsr356.filter.FilterLookup;
import org.jboss.errai.bus.server.websocket.jsr356.filter.WebSocketFilter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.CdiDelegationListener;
import org.jboss.errai.bus.server.websocket.jsr356.weld.filter.CdiFilterLookup;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Michel Werren
 */
@RunWith(Arquillian.class)
public class FilterTest {

  public static final String FILTER_NAMES = "org.jboss.errai.bus.server.websocket.test.jsr356.cdi.filter.FooFilter,org.jboss.errai.bus.server.websocket.test.jsr356.cdi.filter.BarFilter";
  public static final String FAIL_NAMES = "rg.jboss.errai.bus.server.websocket.test.jsr356.cdi.filter.FooFilter,org.jbos.errai.bus.server.websocket.test.jsr356.cdi.filter.BarFilter";

  @Deployment
  public static Archive getDeployment() {
    final WebArchive war = ShrinkWrap.create(WebArchive.class, "filtertest.war");
    war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    war.addClasses(FooFilter.class, BarFilter.class, CdiDelegationListener.class);
    war.addPackages(true, "org.jboss.errai.bus.server.websocket.jsr356");
    final File[] files = Maven.resolver().loadPomFromFile("./pom.xml", "test-dependency-override")
            .resolve("org.jboss.errai:errai-bus:3.0-SNAPSHOT", "com.google.guava:guava:13.0.1").withTransitivity()
            .asFile();
    for (File file : files) {
      war.addAsLibrary(file);
    }
    return war;
  }

  /**
   * Test for the overridden non-jee {@link FilterLookup} and the cdi'fied
   * filters.
   * 
   * @throws Exception
   */
  @Test(expected = RuntimeException.class)
  public void testFilterLookup() throws Exception {
    final StringTokenizer filterTokenizer = new StringTokenizer(FILTER_NAMES, ",");

    // Overriding of the filter should be done already by the servlet context
    // listener
    final FilterLookup filterLookup = FilterLookup.getInstance();
    Assert.assertTrue("Not Cdi filter lookup instance", filterLookup instanceof CdiFilterLookup);

    filterLookup.initFilters(filterTokenizer);
    final List<WebSocketFilter> filters = filterLookup.getFilters();

    boolean fooFilter = false;
    boolean barFilter = false;

    // Test also for proxied classes
    for (WebSocketFilter filter : filters) {
      fooFilter = fooFilter || ((filter instanceof FooFilter) && !filter.getClass().equals(FooFilter.class));
      barFilter = barFilter || ((filter instanceof BarFilter) && !filter.getClass().equals(BarFilter.class));
    }
    Assert.assertTrue("Filter Cdi lookup failed", fooFilter && barFilter);

    final StringTokenizer failTokenizer = new StringTokenizer(FAIL_NAMES, ",");
    // Should fail
    filterLookup.initFilters(failTokenizer);
  }
}
