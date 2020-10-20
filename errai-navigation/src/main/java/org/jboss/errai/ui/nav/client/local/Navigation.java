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

package org.jboss.errai.ui.nav.client.local;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.container.Proxy;
import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleCallback;
import org.jboss.errai.ioc.client.lifecycle.api.StateChange;
import org.jboss.errai.ioc.client.lifecycle.impl.AccessImpl;
import org.jboss.errai.ui.nav.client.local.api.DelegationControl;
import org.jboss.errai.ui.nav.client.local.api.NavigationControl;
import org.jboss.errai.ui.nav.client.local.api.PageNavigationErrorHandler;
import org.jboss.errai.ui.nav.client.local.api.PageNotFoundException;
import org.jboss.errai.ui.nav.client.local.api.RedirectLoopException;
import org.jboss.errai.ui.nav.client.local.pushstate.PushStateUtil;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;
import org.jboss.errai.ui.shared.TemplateWidgetMapper;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Central control point for navigating between pages of the application.
 * <p>
 * Configuration is decentralized: it is based on fields and annotations present in other application classes. This
 * configuration is gathered at compile time.
 *
 * @see Page
 * @see PageState
 * @see PageShowing
 * @see PageShown
 * @see PageHiding
 * @see PageHidden
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@EntryPoint
public class Navigation {

  /**
   * Maximum number of successive redirects until Errai suspects an endless loop.
   */
  final static int MAXIMUM_REDIRECTS = 99;

  /**
   * Encapsulates a navigation request to another page.
   */
  private static class Request<C> {

    PageNode<C> pageNode;

    HistoryToken state;

    /**
     * Construct a new {@link Request}.
     *
     * @param pageNode
     *          The page node to display. Normally, the implementation of PageNode is generated at compile time based on
     *          a Widget subclass that has been annotated with {@code @Page}. Anything calling this method must ensure
     *          that the given PageNode has been entered into the navigation graph, or later navigation back to
     *          {@code toPage} will fail.
     * @param state
     *          The state information to pass to the page node before showing it.
     */
    private Request(final PageNode<C> pageNode, final HistoryToken state) {
      this.pageNode = pageNode;
      this.state = state;
    }

  }

  private final NavigatingContainer navigatingContainer = GWT.create(NavigatingContainer.class);

  protected PageNode<Object> currentPage;

  protected Object currentComponent;

  protected IsWidget currentWidget;

  protected HistoryToken currentPageToken;

  protected ContentDelegation contentDelegation = new DefaultContentDelegation();

  private PageNavigationErrorHandler navigationErrorHandler;

  private HandlerRegistration historyHandlerRegistration;

  private final Map<IsWidget, HandlerRegistration> attachHandlerRegistrations = new HashMap<>();

  @Inject
  private Logger logger;

  /**
   * Indicates that a navigation request is currently processed.
   */
  private boolean locked = false;

  /**
   * Queued navigation requests which could not handled immediately.
   */
  private final Queue<Request> queuedRequests = new LinkedList<>();

  private int redirectDepth = 0;

  @Inject
  private NavigationGraph navGraph;

  @Inject
  private StateChange<Object> stateChangeEvent;

  @Inject
  private HistoryTokenFactory historyTokenFactory;

  @PostConstruct
  private void init() {
    if (navGraph.isEmpty())
      return;

    final String hash = Window.Location.getHash();

    navigationErrorHandler = new DefaultNavigationErrorHandler(this);

    historyHandlerRegistration = HistoryWrapper.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(final ValueChangeEvent<String> event) {
        HistoryToken token = null;
        try {
          logger.debug("URL value changed to " + event.getValue());
          if (needsApplicationContext()) {
            final String context = inferAppContext(event.getValue());
            logger.info("No application context defined. Inferring application context as "
                    + context
                    + ". Change this value by setting the variable \"erraiApplicationWebContext\" in your GWT host page"
                    + ", or calling Navigation.setAppContext.");
            setAppContext(context);
          }
          token = historyTokenFactory.parseURL(event.getValue());

          if (currentPage == null || !token.equals(currentPageToken)) {
            final PageNode<IsWidget> toPage = navGraph.getPage(token.getPageName());
            navigate(new Request<>(toPage, token), false);
          }
        } catch (final Exception e) {
          if (token == null)
            navigationErrorHandler.handleInvalidURLError(e, event.getValue());
          else
            navigationErrorHandler.handleInvalidPageNameError(e, token.getPageName());
        }
      }
    });

    maybeConvertHistoryToken(hash);

    // finally, we bootstrap the navigation system (this invokes the callback
    // above)
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        HistoryWrapper.fireCurrentHistoryState();
      }
    });

  }

  protected String inferAppContext(String url) {
    if (!(url.startsWith("/")))
      url = "/" + url;

    final int indexOfNextSlash = url.indexOf("/", 1);

    if (indexOfNextSlash < 0)
      return "";
    else
      return url.substring(0, indexOfNextSlash);
  }

  /**
   * Set an error handler that is called in case of a {@link PageNotFoundException} error during page navigation.
   *
   * @param handler
   *          An error handler for navigation. Setting this to null assigns the {@link DefaultNavigationErrorHandler}
   */
  public void setErrorHandler(final PageNavigationErrorHandler handler) {
    if (handler == null)
      navigationErrorHandler = new DefaultNavigationErrorHandler(this);
    else
      navigationErrorHandler = handler;
  }

  /**
   * Public for testability.
   */
  @PreDestroy
  public void cleanUp() {
    historyHandlerRegistration.removeHandler();
    setErrorHandler(null);
  }

  /**
   * Looks up the PageNode instance that provides content for the given widget type, sets the state on that page, then
   * makes the widget visible in the content area.
   *
   * @param toPage
   *          The content type of the page node to look up and display. Normally, this is a Widget subclass that has
   *          been annotated with {@code @Page}.
   * @param state
   *          The state information to set on the page node before showing it. Normally the map keys correspond with the
   *          names of fields annotated with {@code @PageState} in the widget class, but this is not required.
   */
  public <C> void goTo(final Class<C> toPage, final Multimap<String, String> state) {
    PageNode<C> toPageInstance = null;

    try {
      toPageInstance = navGraph.getPage(toPage);
      navigate(toPageInstance, state);
    } catch (final RedirectLoopException e) {
      throw e;
    } catch (final RuntimeException e) {
      if (toPageInstance == null)
        // This is an extremely unlikely case, so throwing an exception is preferable to going through the navigation error handler.
        throw new PageNotFoundException("There is no page of type " + toPage.getName() + " in the navigation graph.");
      else
        navigationErrorHandler.handleInvalidPageNameError(e, toPageInstance.name());
    }

  }

  /**
   * Same as {@link #goTo(Class, Multimap)} but then with the page name.
   *
   * @param toPage
   *          the name of the page node to lookup and display.
   */
  public void goTo(final String toPage) {
    PageNode<?> toPageInstance = null;
    try {
      toPageInstance = navGraph.getPage(toPage);
      navigate(toPageInstance);
    } catch (final RedirectLoopException e) {
      throw e;
    } catch (final RuntimeException e) {
      navigationErrorHandler.handleInvalidPageNameError(e, toPage);
    }
  }

  /**
   * Looks up the PageNode instance of the page that has the unique role set and makes the widget visible in the content
   * area.
   *
   * @param role
   *          The unique role of the page that needs to be displayed.
   */
  public void goToWithRole(final Class<? extends UniquePageRole> role) {
    PageNode<?> toPageInstance = null;
    try {
      toPageInstance = navGraph.getPageByRole(role);
      navigate(toPageInstance);
    } catch (final RedirectLoopException e) {
      throw e;
    } catch (final RuntimeException e) {
      navigationErrorHandler.handleError(e, role);
    }
  }

  /**
   * Return all PageNode instances that have specified pageRole.
   *
   * @param pageRole
   *          the role to find PageNodes by
   * @return All the pageNodes of the pages that have the specific pageRole.
   */
  public Collection<PageNode<?>> getPagesByRole(final Class<? extends PageRole> pageRole) {
    return navGraph.getPagesByRole(pageRole);
  }

  private <C> void navigate(final PageNode<C> toPageInstance) {
    navigate(toPageInstance, ImmutableListMultimap.<String, String> of());
  }

  private <C> void navigate(final PageNode<C> toPageInstance, final Multimap<String, String> state) {
    final HistoryToken token = historyTokenFactory.createHistoryToken(toPageInstance.name(), state);
    logger.debug("Navigating to " + toPageInstance.name() + " at url: " + token.toString());
    navigate(new Request<>(toPageInstance, token), true);
  }

  /**
   * Captures a backup of the current page state in history, sets the state on the given PageNode from the given state
   * token, then makes its widget visible in the content area.
   */
  private <C> void navigate(final Request<C> request, final boolean fireEvent) {
    if (locked) {
      queuedRequests.add(request);
      return;
    }

    redirectDepth++;
    if (redirectDepth >= MAXIMUM_REDIRECTS) {
      throw new RedirectLoopException("Maximum redirect limit of " + MAXIMUM_REDIRECTS + " reached. "
              + "Do you have a redirect loop?");
    }

    maybeShowPage(request, fireEvent);
  }

  private <C> void handleQueuedRequests(final Request<C> request, final boolean fireEvent) {
    if (queuedRequests.isEmpty()) {
      // No new navigation requests were recorded in the lifecycle methods.
      // This is the page which has to be displayed and the browser's history
      // can be updated.
      redirectDepth = 0;
      HistoryWrapper.newItem(request.state.toString(), fireEvent);
    }
    else {
      // Process all navigation requests captured in the lifecycle methods.
      while (queuedRequests.size() != 0) {
        navigate(queuedRequests.poll(), fireEvent);
      }
    }
  }

  /**
   * Attach the content panel to the RootPanel if does not already have a parent.
   */
  private void maybeAttachContentPanel() {
    if (getContentPanel().asWidget().getParent() == null) {
      RootPanel.get().add(getContentPanel());
    }
  }

  /**
   * Hide the page currently displayed and call the associated lifecycle methods.
   *
   * @param requestPage the next requested page, this can be null if there is none.
   */
  private void hideCurrentPage(Object requestPage, NavigationControl control) {
    final IsWidget currentContent = navigatingContainer.getWidget();

    // Note: Optimized out in production mode
    if (currentPage != null && (currentContent == null || currentWidget.asWidget() != currentContent)) {
      // This could happen if someone was manipulating the DOM behind our backs
      GWT.log("Current content widget vanished or changed. " + "Not delivering pageHiding event to " + currentPage
            + ".");
    }

    DelegationControl hideControl = new DelegationControl(() -> {
      if (currentPage != null && currentComponent != null) {
        currentPage.pageHidden(currentComponent);
        currentPage.destroy(currentComponent);
      }

      control.proceed();
    });

    if (currentComponent != null) {
      contentDelegation.hideContent(currentComponent, navigatingContainer, currentWidget, requestPage, hideControl);
    } else {
      navigatingContainer.clear();
      hideControl.proceed();
    }
  }

  /**
   * Call navigation and page related lifecycle methods.
   * If the {@link Access} is fired successfully, load the new page.
   */
  private <C> void maybeShowPage(final Request<C> request, final boolean fireEvent) {
    request.pageNode.produceContent(component -> {
      if (component == null) {
        throw new NullPointerException("Target page " + request.pageNode + " returned a null content widget");
      }

      final C unwrappedComponent = Factory.maybeUnwrapProxy(component);
      final Widget widget;
      if (unwrappedComponent instanceof IsWidget) {
        widget = ((IsWidget) unwrappedComponent).asWidget();
      }
      else if (unwrappedComponent instanceof IsElement) {
        widget = ElementWrapperWidget.getWidget(((IsElement) unwrappedComponent).getElement());
      }
      else if (unwrappedComponent instanceof org.jboss.errai.common.client.api.elemental2.IsElement) {
        widget = ElementWrapperWidget.getWidget(((org.jboss.errai.common.client.api.elemental2.IsElement) unwrappedComponent).getElement(), null);
      }
      else if (TemplateWidgetMapper.containsKey(unwrappedComponent)) {
        widget = TemplateWidgetMapper.get(unwrappedComponent);
      }
      else {
        throw new RuntimeException("Page must implement IsWidget, IsElement, or be @Templated.");
      }

      maybeAttachContentPanel();
      currentPageToken = request.state;

      if ((unwrappedComponent instanceof Composite) && (getCompositeWidget((Composite) unwrappedComponent) == null)) {
        final HandlerRegistration reg = widget.addAttachHandler(new Handler() {
          @Override
          public void onAttachOrDetach(final AttachEvent event) {
            if (event.isAttached() && currentWidget != unwrappedComponent) {
              pageHiding(unwrappedComponent, widget, request, fireEvent);
            }
          }
        });
        attachHandlerRegistrations.put(widget, reg);
      }
      else {
        pageHiding(unwrappedComponent, widget, request, fireEvent);
      }
    });
  }

  private <C, W extends IsWidget> void pageHiding(final C component, final W componentWidget, final Request<C> request, final boolean fireEvent) {
    if (component instanceof Proxy) {
      throw new RuntimeException("Was passed in a proxy, but should always receive an unwrapped widget.");
    }

    final HandlerRegistration reg = attachHandlerRegistrations.remove(component);
    if (reg != null) {
      reg.removeHandler();
    }

    final NavigationControl control = new NavigationControl(Navigation.this, new Runnable() {
      @Override
      public void run() {
        final Access<C> accessEvent = new AccessImpl<>();

        accessEvent.fireAsync(component, new LifecycleCallback() {
          @Override
          public void callback(final boolean success) {
            if (success) {
              NavigationControl showControl = new NavigationControl(Navigation.this, new Runnable() {
                @Override
                public void run() {
                  try {
                    // Fire IOC lifecycle event to indicate that the state of the
                    // bean has changed.
                    // TODO make this smarter and only fire state change event when
                    // fields actually changed.
                    stateChangeEvent.fireAsync(component);

                    Object previousPage = currentComponent;
                    setCurrentPage(request.pageNode);
                    currentWidget = componentWidget;
                    currentComponent = component;
                    contentDelegation.showContent(component, navigatingContainer, currentWidget, previousPage, new DelegationControl(() -> {
                      request.pageNode.pageShown(component, request.state);
                    }));
                  } finally {
                    locked = false;
                  }

                  handleQueuedRequests(request, fireEvent);
                }
              }, new Runnable() {
                @Override
                public void run() {
                  locked = false;
                }
              });

              try {
                locked = true;
                hideCurrentPage(component, new NavigationControl(Navigation.this, () -> {
                  request.pageNode.pageShowing(component, request.state, showControl);
                }));
              } catch (Exception ex) {
                locked = false;
                throw ex;
              }
            } else {
              request.pageNode.destroy(component);
            }
          }
        });
      }
    }, new Runnable() {
      @Override
      public void run() {
        hideCurrentPage(null, new NavigationControl(Navigation.this, () -> {
          setCurrentPage(null);
        }));
      }
    });

    if (currentPage != null && currentWidget != null && currentComponent != null && currentWidget.asWidget() == navigatingContainer.getWidget()) {
      currentPage.pageHiding(Factory.maybeUnwrapProxy(currentComponent), control);
    } else {
      control.proceed();
    }
  }

  /**
   * Return the current page that is being displayed.
   *
   * @return the current page
   */
  public PageNode<?> getCurrentPage() {
    return currentPage;
  }

  /**
   * @return The state multimap used to show the currently displayed page. If a navigation request has been submitted
   *         this may return the state of page being navigated to before that page has actually been displayed.
   */
  public Multimap<String, String> getCurrentState() {
    return (currentPageToken != null) ? currentPageToken.getState() : ImmutableMultimap.of();
  }

  /**
   * Returns the panel that this Navigation object manages. The contents of this panel will be updated by the navigation
   * system in response to PageTransition requests, as well as changes to the GWT navigation system.
   *
   * @return The content panel of this Navigation instance. It is not recommended that client code modifies the contents
   *         of this panel, because this Navigation instance may replace its contents at any time.
   */
  public IsWidget getContentPanel() {
    return navigatingContainer.asWidget();
  }

  /**
   * Returns the navigation graph that provides PageNode instances to this Navigation instance.
   */
  // should this method be public? should we expose a way to set the nav graph?
  NavigationGraph getNavGraph() {
    return navGraph;
  }

  /**
   * Just sets the currentPage field. This method exists primarily to get around a generics Catch-22.
   *
   * @param currentPage
   *          the new value for currentPage.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void setCurrentPage(final PageNode currentPage) {
    this.currentPage = currentPage;
  }

  private boolean needsApplicationContext() {
    return (currentPage == null) && (PushStateUtil.isPushStateActivated()) && (getAppContextFromHostPage() == null);
  }

  /**
   * Sets the application context used in pushstate URL paths. This application context should match the deployed
   * application context in your web.xml
   *
   * @param path The context path. Never null.
   */
  public static native void setAppContext(String path) /*-{
      if (path == null) {
          $wnd.erraiApplicationWebContext = undefined;
      }
      else {
          $wnd.erraiApplicationWebContext = path;
      }
  }-*/;

  /**
   * Gets the application context used in pushstate URL paths. This application context should match the deployed
   * application context in your web.xml
   *
   * @return The application context. This may return the empty String (but never null). If non-empty, the return value
   *         always starts with a slash and never ends with one.
   */
  public static String getAppContext() {
    if (PushStateUtil.isPushStateActivated())
      return getAppContextFromHostPage();
    else
      return "";
  }

  private static String getAppContextFromHostPage() {
    String context = getRawAppContextFromHostPage();
    if (!context.isEmpty() && !context.startsWith("/")) {
      context = "/" + context;
    }
    if (context.endsWith("/")) {
      context = context.substring(0, context.length()-1);
    }

    return context;
  }

  private static native String getRawAppContextFromHostPage() /*-{
      if ($wnd.erraiApplicationWebContext === undefined || $wnd.erraiApplicationWebContext.length === 0) {
          return "";
      }
      else {
          return $wnd.erraiApplicationWebContext;
      }
  }-*/;

  private void maybeConvertHistoryToken(String token) {
    if (PushStateUtil.isPushStateActivated()) {
      if (token == null || token.isEmpty()) {
        return;
      }

      if (token.startsWith("#")) {
        token = token.substring(1);
      }

      HistoryWrapper.newItem(Window.Location.getPath() + token, false);
    }
  }

  private native static IsWidget getCompositeWidget(Composite instance) /*-{
      return instance.@com.google.gwt.user.client.ui.Composite::widget;
  }-*/;

  /**
   * Update the state of your existing page without performing a full navigation.
   * <br/>
   * This will perform a pseudo navigation updating the history token with the new states.
   */
  public void updateState(Multimap<String, String> state) {
    if(currentPage != null) {
      currentPageToken = historyTokenFactory.createHistoryToken(currentPage.name(), state);
      HistoryWrapper.newItem(currentPageToken.toString(), false);
      currentPage.pageUpdate(currentComponent, currentPageToken);
    } else {
      logger.error("Cannot update the state before a page has loaded.");
    }
  }

  /**
   * Are we in the navigation process right now.
   */
  public boolean isNavigating() {
    return locked;
  }

  public void setContentDelegation(ContentDelegation contentDelegation) {
    this.contentDelegation = contentDelegation;
  }
}
