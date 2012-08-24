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
  private final Class<? extends Page> fromPage;
  private final Class<P> toPage;
  private final String reason;

  /**
   * Creates a new PageTransition with the given attributes.
   *
   * @param navigation
   *          The navigation system this page transition participates in.
   * @param fromPage
   *          The page type this transition starts at. Not null.
   * @param toPage
   *          The page type this transition goes to. Not null.
   * @param reason
   *          The label that should be given to this transition; the reason the
   *          user is moving from the {@link #fromPage()} to the
   *          {@link #toPage()}. Not null.
   * @throws NullPointerException
   *           if any of the arguments are null.
   */
  PageTransition(Navigation navigation, Class<? extends Page> fromPage, Class<P> toPage, String reason) {
    this.navigation = Assert.notNull(navigation);
    this.fromPage = Assert.notNull(fromPage);
    this.toPage = Assert.notNull(toPage);
    this.reason = Assert.notNull(reason);
  }

  /**
   * The page this transition starts at.
   */
  public Class<? extends Page> fromPage() {
    return fromPage;
  }

  /**
   * The page this transition goes to.
   */
  public Class<P> toPage() {
    return toPage;
  }

  /**
   * The label that should be given to this transition; the reason the user is
   * moving from the {@link #fromPage()} to the {@link #toPage()}.
   */
  String reason() {
    return reason;
  }

  /**
   * Transitions the application's view from the current page (whatever it is)
   * to the {@code toPage} of this transition.
   */
  public void go(String ... pathParams) {
    navigation.goTo(toPage, pathParams);
  }
}
