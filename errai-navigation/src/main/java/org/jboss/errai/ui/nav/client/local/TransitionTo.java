package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.user.client.ui.IsWidget;

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
public final class TransitionTo<P extends IsWidget> {

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
  TransitionTo(Class<P> toPage) {
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
   * <p>
   * Note: if the Navigation framework is being used together with ErraiIOC in
   * asynchronous mode, the page transition may not have happened by the
   * time this method returns.
   */
  public void go() {
    go(ImmutableMultimap.<String, String>of());
  }

  /**
   * Transitions the application's view from the current page (whatever it is)
   * to the {@code toPage} of this transition, passing the given extra state
   * information.
   * <p>
   * Note: if the Navigation framework is being used together with ErraiIOC in
   * asynchronous mode, the page transition may not have happened by the
   * time this method returns.
   *
   * @param state
   *          Extra state information that should be passed to the page before
   *          it is displayed. Must not be null.
   */
  public void go(final Multimap<String,String> state) {
    IOC.getAsyncBeanManager().lookupBean(Navigation.class).getInstance(
            new CreationalCallback<Navigation>() {
      @Override
      public void callback(Navigation navigation) {
        navigation.goTo(toPageWidgetType, state);
      }
    });
  }

}
