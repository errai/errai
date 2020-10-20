/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Michel Werren
 */
@Ignore //FIXME: Remove this @Ignore and fix issues with arquillian/WildFly14
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
            .resolve("org.jboss.errai:errai-bus:?", "com.google.guava:guava:?").withTransitivity()
            .asFile();
    for (final File file : files) {
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
    for (final WebSocketFilter filter : filters) {
      fooFilter = fooFilter || ((filter instanceof FooFilter) && !filter.getClass().equals(FooFilter.class));
      barFilter = barFilter || ((filter instanceof BarFilter) && !filter.getClass().equals(BarFilter.class));
    }
    Assert.assertTrue("Filter Cdi lookup failed", fooFilter && barFilter);

    final StringTokenizer failTokenizer = new StringTokenizer(FAIL_NAMES, ",");
    // Should fail
    filterLookup.initFilters(failTokenizer);
  }
}
