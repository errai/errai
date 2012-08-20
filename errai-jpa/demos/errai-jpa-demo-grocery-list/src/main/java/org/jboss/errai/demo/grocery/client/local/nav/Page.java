package org.jboss.errai.demo.grocery.client.local.nav;

import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a page (a distinct place that can be navigated to and bookmarked
 * to return to later).
 * <p>
 * Thinking of the application flow as a directed graph, Pages are the nodes and
 * {@link PageTransition}s are the edges.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface Page {

  /**
   * Returns the name of this page.
   *
   * @return This page's name. Never null.
   */
  public String name();

  /**
   * Returns the widget that provides this page's content.
   *
   * @return The widget to display for this page. Never null.
   */
  public Widget content();
}
