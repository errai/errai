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

package org.jboss.errai.ui.nav.client.local;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.gwt.core.client.GWT;

/**
 * This class is responsible for initializing and providing the {@link URLPatternMatcher} for the app.
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
public class URLPatternMatcherProvider {

  @Produces @ApplicationScoped
  public URLPatternMatcher createURLPatternMatcher(NavigationGraph navGraph) {
    URLPatternMatcher patternMatcher = new URLPatternMatcher();
    Collection<PageNode<?>> pages = navGraph.getAllPages();

    for(PageNode<?> page : pages) {
      patternMatcher.add(page.getURL(), page.name());
    }

    if (!navGraph.isEmpty()) {
      PageNode<?> defaultPageNode = navGraph.getPageByRole(DefaultPage.class);
      patternMatcher.setAsDefaultPage(defaultPageNode.name());
    }
    return patternMatcher;
  }

  @Produces @ApplicationScoped
  public NavigationGraph createNavigationGraph() {
    return GWT.create(NavigationGraph.class);
  }
}
