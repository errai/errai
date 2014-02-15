package org.jboss.errai.ui.nav.client.local.spi;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ui.nav.client.local.HistoryToken;
import org.jboss.errai.ui.nav.client.local.TransitionTo;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Represents a page (a distinct place that can be navigated to and bookmarked
 * to return to later).
 * <p>
 * Thinking of the application flow as a directed graph, Pages are the nodes and
 * {@link TransitionTo}s are the edges.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface PageNode<W extends IsWidget> {

  /**
   * Returns the name of this page.
   *
   * @return This page's name. Never null.
   */
  public String name();

  /**
   * Retrieves the widget that provides this page's content from the client-side
   * bean manager.
   *
   * @param callback
   *          The callback that will receive the widget to display for this
   *          page. The Widget will have the same runtime type as returned by
   *          {@link #contentType()}, and will never be null.
   */
  public void produceContent(CreationalCallback<W> callback);

  /**
   * Returns the type of widget that this page node's
   * {@link #produceContent(CreationalCallback)} method will produce.
   *
   * @return The type of widget that supplies this page's content. Never null.
   */
  public Class<W> contentType();

  /**
   * Called by the framework when this page node is about to be displayed in the
   * navigation content panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is
   * undefined.
   *
   * @param widget
   *          the widget instance that was just returned from a call to
   *          {@link #produceContent(CreationalCallback)}. Never null.
   * @param state
   *          the state of the page, parsed from the history token on the URL.
   *          Never null.
   */
  public void pageShowing(W widget, HistoryToken state);

  /**
   * Called by the framework when this page node was displayed in the
   * navigation content panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is
   * undefined.
   *
   * @param widget
   *          the widget instance that was just returned from a call to
   *          {@link #produceContent(CreationalCallback)}. Never null.
   * @param state
   *          the state of the page, parsed from the history token on the URL.
   *          Never null.
   */
  public void pageShown(W widget, HistoryToken state);

  /**
   * Called by the framework when this page node is about to be removed
   * from the navigation content panel.
   * <p>
   * If this method returns null or throws an exception when called, framework
   * behaviour is undefined.
   *
   * @param widget
   *          the widget instance (which is currently in the navigation content
   *          panel) that was previously used in the call to
   *          {@link #pageShowing(IsWidget, HistoryToken)}. Never null.
   */
  public void pageHiding(W widget);

  /**
   * Called by the framework after this page has been removed from the
   * navigation content panel.
   * <p>
   * If this method returns null or throws an exception when called, framework
   * behaviour is undefined.
   *
   * @param widget
   *          the widget instance (which was in the navigation content
   *          panel) that was previously used in the call to
   *          {@link #pageShowing(IsWidget, HistoryToken)}. Never null.
   */
  public void pageHidden(W widget);

}
