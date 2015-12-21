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

package org.jboss.errai.ui.nav.client.local.spi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ui.nav.client.local.HistoryToken;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.nav.client.local.api.NavigationControl;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Represents a page (a distinct place that can be navigated to and bookmarked to return to later).
 * <p>
 * Thinking of the application flow as a directed graph, Pages are the nodes and {@link TransitionTo}s are the edges.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface PageNode<C> {

  /**
   * Returns the name of this page.
   *
   * @return This page's name. Never null.
   */
  public String name();

  /**
   * Returns the URL template specified for this page by {@link Page#path()}. If no template is specified, it returns the page name.
   *
   * @return This page's URL. Never null.
   */
  public String getURL();

  /**
   * Retrieves the widget that provides this page's content from the client-side bean manager.
   *
   * @param callback
   *          The callback that will receive the widget to display for this page. The Widget will have the same runtime
   *          type as returned by {@link #contentType()}, and will never be null.
   */
  public void produceContent(CreationalCallback<C> callback);

  /**
   * Returns the type of widget that this page node's {@link #produceContent(CreationalCallback)} method will produce.
   *
   * @return The type of widget that supplies this page's content. Never null.
   */
  public Class<C> contentType();

  /**
   * Called by the framework when this page node is about to be displayed in the navigation content panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is undefined.
   *
   * @param widget
   *          the widget instance that was just returned from a call to {@link #produceContent(CreationalCallback)}.
   *          Never null.
   * @param state
   *          the state of the page, parsed from the history token on the URL. Never null.
   */
  public void pageShowing(C widget, HistoryToken state);

  /**
   * Called by the framework when this page node was displayed in the navigation content panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is undefined.
   *
   * @param widget
   *          the widget instance that was just returned from a call to {@link #produceContent(CreationalCallback)}.
   *          Never null.
   * @param state
   *          the state of the page, parsed from the history token on the URL. Never null.
   */
  public void pageShown(C widget, HistoryToken state);

  /**
   * Called by the framework when this page node is about to be removed from the navigation content panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is undefined.
   *
   * @param widget
   *          the widget instance (which is currently in the navigation content panel) that was previously used in the
   *          call to {@link #pageShowing(IsWidget, HistoryToken)}. Never null.
   */
  public void pageHiding(C widget, NavigationControl control);

  /**
   * Called by the framework after this page has been removed from the navigation content panel.
   * <p>
   * If this method throws an exception when called, framework behaviour is undefined.
   *
   * @param widget
   *          the widget instance (which was in the navigation content panel) that was previously used in the call to
   *          {@link #pageShowing(IsWidget, HistoryToken)}. Never null.
   */
  public void pageHidden(C widget);

  /**
   * Used by the framework to destroy {@link Dependent} scoped beans after a page is no longer needed. For
   * {@link ApplicationScoped} beans this method is a noop.
   *
   * @param widget
   *          The widget instance that will be destroyed if it is a dependent-scoped bean. Never null.
   */
  public void destroy(C widget);

}
