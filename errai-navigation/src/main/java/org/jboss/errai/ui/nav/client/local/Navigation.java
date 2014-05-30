package org.jboss.errai.ui.nav.client.local;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleCallback;
import org.jboss.errai.ioc.client.lifecycle.api.StateChange;
import org.jboss.errai.ioc.client.lifecycle.impl.AccessImpl;
import org.jboss.errai.ui.nav.client.local.api.NavigationControl;
import org.jboss.errai.ui.nav.client.local.api.PageNavigationErrorHandler;
import org.jboss.errai.ui.nav.client.local.api.PageNotFoundException;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.HandlerRegistration;

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
  private static class Request<W extends IsWidget> {

    PageNode<W> pageNode;

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
    private Request(PageNode<W> pageNode, HistoryToken state) {
      this.pageNode = pageNode;
      this.state = state;
    }

  }

  private final NavigatingContainer navigatingContainer = GWT.create(NavigatingContainer.class);

  private final NavigationGraph navGraph = GWT.create(NavigationGraph.class);

  protected PageNode<IsWidget> currentPage;

  protected IsWidget currentWidget;

  private HandlerRegistration historyHandlerRegistration;

  private PageNavigationErrorHandler navigationErrorHandler = new DefaultNavigationErrorHandler(this);

  /**
   * Indicates that a navigation request is currently processed.
   */
  private boolean locked = false;

  /**
   * Queued navigation requests which could not handled immediately.
   */
  private final Queue<Request> queuedRequests = new LinkedList<Request>();

  private int redirectDepth = 0;

  @Inject
  private StateChange<Object> stateChangeEvent;

  @PostConstruct
  private void init() {
    if (navGraph.isEmpty())
      return;

    historyHandlerRegistration = History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(final ValueChangeEvent<String> event) {
        HistoryToken token = HistoryToken.parse(event.getValue());
        PageNode<IsWidget> toPage = null;

        try {
          toPage = navGraph.getPage(token.getPageName());
          if (currentPage == null || !toPage.name().equals(currentPage.name())) {
            navigate(new Request<IsWidget>(toPage, token), false);
          }
        } catch (Exception e) {
          navigationErrorHandler.handleError(e, token.getPageName());
        }
      }
    });

    // finally, we bootstrap the navigation system (this invokes the callback
    // above)
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        History.fireCurrentHistoryState();
      }
    });

  }

  /**
   * Set an error handler that is called in case of a {@link PageNotFoundException} error during page navigation.
   * 
   * @param handler
   *          An error handler for navigation. Setting this to null assigns the {@link DefaultNavigationErrorHandler}
   */
  public void setErrorHandler(PageNavigationErrorHandler handler) {
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
  public <W extends IsWidget> void goTo(Class<W> toPage, Multimap<String, String> state) {
    PageNode<W> toPageInstance = navGraph.getPage(toPage);
    navigate(toPageInstance, state);
  }

  /**
   * Same as {@link #goTo(Class, com.google.common.collect.Multimap)} but then with the page name.
   * 
   * @param toPage
   *          the name of the page node to lookup and display.
   */
  public void goTo(String toPage) {
    PageNode<? extends IsWidget> toPageInstance = null;
    try {
      toPageInstance = navGraph.getPage(toPage);
      navigate(toPageInstance);
    } catch (Exception e) {
      navigationErrorHandler.handleError(e, toPage);
    }
  }

  /**
   * Looks up the PageNode instance of the page that has the unique role set and makes the widget visible in the content
   * area.
   * 
   * @param role
   *          The unique role of the page that needs to be displayed.
   */
  public void goToWithRole(Class<? extends UniquePageRole> role) {
    try {
      PageNode<?> toPageInstance = navGraph.getPageByRole(role);
      navigate(toPageInstance);
    } catch (Exception e) {
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
  public Collection<PageNode<? extends IsWidget>> getPagesByRole(Class<? extends PageRole> pageRole) {
    return navGraph.getPagesByRole(pageRole);
  }

  private <W extends IsWidget> void navigate(PageNode<W> toPageInstance) {
    navigate(toPageInstance, ImmutableListMultimap.<String, String> of());
  }

  private <W extends IsWidget> void navigate(PageNode<W> toPageInstance, Multimap<String, String> state) {
    HistoryToken token = HistoryToken.of(toPageInstance.name(), state);
    navigate(new Request<W>(toPageInstance, token), true);
  }

  /**
   * Captures a backup of the current page state in history, sets the state on the given PageNode from the given state
   * token, then makes its widget visible in the content area.
   */
  private <W extends IsWidget> void navigate(Request<W> request, boolean fireEvent) {
    if (locked) {
      queuedRequests.add(request);
      return;
    }

    redirectDepth++;
    if (redirectDepth >= MAXIMUM_REDIRECTS) {
      throw new RuntimeException("Maximum redirect limit of " + MAXIMUM_REDIRECTS + " reached. "
              + "Do you have a redirect loop?");
    }

    maybeShowPage(request, fireEvent);
  }

  private <W extends IsWidget> void handleQueuedRequests(Request<W> request, boolean fireEvent) {
    if (queuedRequests.isEmpty()) {
      // No new navigation requests were recorded in the lifecycle methods.
      // This is the page which has to be displayed and the browser's history
      // can be updated.
      redirectDepth = 0;
      History.newItem(request.state.toString(), fireEvent);
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
   */
  private void hideCurrentPage() {
    IsWidget currentContent = navigatingContainer.getWidget();

    // Note: Optimized out in production mode
    if (currentPage != null && (currentContent == null || currentWidget.asWidget() != currentContent)) {
      // This could happen if someone was manipulating the DOM behind our backs
      GWT.log("Current content widget vanished or changed. " + "Not delivering pageHiding event to " + currentPage
              + ".");
    }

    // Ensure clean contentPanel regardless of currentPage being null
    navigatingContainer.clear();

    if (currentPage != null && currentWidget != null) {
      currentPage.pageHidden(currentWidget);
      currentPage.destroy(currentWidget);
    }
  }

  /**
   * Call navigation and page related lifecycle methods. If the {@link Access} is fired successfully, load the new page.
   */
  private <W extends IsWidget> void maybeShowPage(final Request<W> request, final boolean fireEvent) {
    request.pageNode.produceContent(new CreationalCallback<W>() {
      @Override
      public void callback(final W widget) {
        if (widget == null) {
          throw new NullPointerException("Target page " + request.pageNode + " returned a null content widget");
        }
        maybeAttachContentPanel();
        pageHiding(widget, request, fireEvent);
      }
    });
  }

  private <W extends IsWidget> void pageHiding(final W widget, final Request<W> request, final boolean fireEvent) {
    final NavigationControl control = new NavigationControl(new Runnable() {

      @Override
      public void run() {
        final Access<W> accessEvent = new AccessImpl<W>();
        accessEvent.fireAsync(widget, new LifecycleCallback() {

          @Override
          public void callback(final boolean success) {
            if (success) {
              locked = true;
              try {
                hideCurrentPage();
                request.pageNode.pageShowing(widget, request.state);

                // Fire IOC lifecycle event to indicate that the state of the
                // bean has changed.
                // TODO make this smarter and only fire state change event when
                // fields actually changed.
                stateChangeEvent.fireAsync(widget);

                setCurrentPage(request.pageNode);
                currentWidget = widget;
                navigatingContainer.setWidget(widget);
                request.pageNode.pageShown(widget, request.state);
              } finally {
                locked = false;
              }

              handleQueuedRequests(request, fireEvent);
            }
            else {
              request.pageNode.destroy(widget);
            }
          }
        });
      }
    });

    if (currentPage != null && currentWidget != null && currentWidget.asWidget() == navigatingContainer.getWidget()) {
      currentPage.pageHiding(currentWidget, control);
    }
    else {
      control.proceed();
    }
  }

  /**
   * Return the current page that is being displayed.
   * 
   * @return the current page
   */
  public PageNode<IsWidget> getCurrentPage() {
    return currentPage;
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
  private void setCurrentPage(PageNode currentPage) {
    this.currentPage = currentPage;
  }
}
