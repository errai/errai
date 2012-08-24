package org.jboss.errai.demo.grocery.client.local.nav;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.demo.grocery.client.local.Navigation;

/**
 * Represents navigability from one page to another in the application's flow.
 * Thinking of the application flow as a directed graph, {@link Page}s are the
 * nodes and PageTransitions are the edges.
 * <p>
 * To create an instance of this class, use the {@code Page#addTransition} method.
 * <p>
 * Instances of this class are immutable.
 *
 * @param <P> The type of the target page ("to page")
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public final class PageTransition<P extends Page> {

  private final Navigation navigation;
  private final Class<P> toPage;

  /**
   * Creates a new PageTransition with the given attributes.
   *
   * @param navigation
   *          The navigation system this page transition participates in.
   * @param toPage
   *          The page type this transition goes to. Not null.
   * @throws NullPointerException
   *           if any of the arguments are null.
   */
  PageTransition(Navigation navigation, Class<P> toPage) {
    this.navigation = Assert.notNull(navigation);
    this.toPage = Assert.notNull(toPage);
  }

  /**
   * The page this transition goes to.
   */
  public Class<P> toPage() {
    return toPage;
  }

  /**
   * Transitions the application's view from the current page (whatever it is)
   * to the {@code toPage} of this transition.
   */
  public void go(String ... pathParams) {
    navigation.goTo(toPage, pathParams);
  }
}
