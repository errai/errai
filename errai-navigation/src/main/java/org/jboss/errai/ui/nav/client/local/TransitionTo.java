package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents navigability from one page to another in the application's flow.
 * Thinking of the application flow as a directed graph, {@link PageNode}s are the
 * nodes and PageTransitions are the edges.
 * <p>
 * Instances of this class are normally obtained via dependency injection.
 * <p>
 * Instances of this class are immutable.
 *
 * @param <P> The type of the target page ("to page")
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public final class TransitionTo<P extends Widget> {

  private final Navigation navigation;
  private final Class<P> toPageWidgetType;

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
  TransitionTo(Navigation navigation, Class<P> toPage) {
    this.navigation = Assert.notNull(navigation);
    this.toPageWidgetType = Assert.notNull(toPage);
  }

  /**
   * The page this transition goes to.
   */
  public Class<P> toPageType() {
    return toPageWidgetType;
  }

  /**
   * Transitions the application's view from the current page (whatever it is)
   * to the {@code toPage} of this transition, passing no extra state
   * information.
   */
  public void go() {
    navigation.goTo(toPageWidgetType, ImmutableMultimap.<String,String>of());
  }

  /**
   * Transitions the application's view from the current page (whatever it is)
   * to the {@code toPage} of this transition, passing the given extra state
   * information.
   *
   * @param state
   *          Extra state information that should be passed to the page before
   *          it is displayed. Must not be null.
   */
  public void go(Multimap<String,String> state) {
    navigation.goTo(toPageWidgetType, state);
  }

}
