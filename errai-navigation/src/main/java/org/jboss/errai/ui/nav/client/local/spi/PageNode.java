package org.jboss.errai.ui.nav.client.local.spi;

import org.jboss.errai.ui.nav.client.local.TransitionTo;

import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a page (a distinct place that can be navigated to and bookmarked
 * to return to later).
 * <p>
 * Thinking of the application flow as a directed graph, Pages are the nodes and
 * {@link TransitionTo}s are the edges.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface PageNode {

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
   * @return The widget to display for this page. The Widget returned will have
   *         the exact runtime type as returned by {@link #contentType()}. Never
   *         null.
   */
  public Widget content();

  /**
   * Returns the type of widget that this page node's {@link #content()} method will produce.
   *
   * @return The type of widget that supplies this page's content. Never null.
   */
  public Class<? extends Widget> contentType();
}
