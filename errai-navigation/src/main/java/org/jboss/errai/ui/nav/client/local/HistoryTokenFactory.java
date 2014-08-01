package org.jboss.errai.ui.nav.client.local;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * All HistoryToken instances are produced by this class.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
@ApplicationScoped
public class HistoryTokenFactory {
  private final URLPatternMatcher patternMatcher;

  @Inject
  public HistoryTokenFactory(URLPatternMatcher upm) {
    this.patternMatcher = upm;
  }

  /**
   * This can be used to create a HistoryToken when navigating by page name.
   * 
   * @param pageName
   *          The name of the page. Never null.
   * @param state
   *          The map of {@link PageState} keys and values. Never null.
   * @return A HistoryToken with the parsed URL matching information.
   */
  public HistoryToken createHistoryToken(String pageName, Multimap<String, String> state) {
    URLPattern pattern = patternMatcher.getURLPattern(pageName);
    return new HistoryToken(pageName, ImmutableMultimap.copyOf(state), pattern);
  }

  /**
   * This can be used to generate a HistoryToken from a URL path
   * 
   * @param url
   *          The typed URL path. If the browser is pushstate-enabled, this will be the URI path, otherwise it will be
   *          the fragment identifier.
   * @return A HistoryToken with the parsed URL matching information.
   */
  public HistoryToken parseURL(String url) {
    // Remove the leading slash from the context and the URL for pushstate/non-pushstate URL compatibility.
    String context = Navigation.getAppContext();
    if ((!(context.equals(""))) && (context.startsWith("/")))
      context = context.substring(1);
    
    if (url.startsWith("/")) {
      url = url.substring(1);
    }
    
    if (url.startsWith(context)) {
      url = url.substring(context.length());
    }

    return patternMatcher.parseURL(url);
  }
}
