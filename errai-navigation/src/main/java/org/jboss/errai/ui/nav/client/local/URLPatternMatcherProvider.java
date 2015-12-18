package org.jboss.errai.ui.nav.client.local;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;

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
    Collection<PageNode<? extends IsWidget>> pages = navGraph.getAllPages();
    
    for(PageNode<? extends IsWidget> page : pages) {
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
