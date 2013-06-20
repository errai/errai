package org.jboss.errai.ui.nav.client.local;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Central control point for navigating between pages of the application.
 * <p>
 * Configuration is decentralized: it is based on fields and annotations present
 * in other application classes. This configuration is gathered at compile time.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@ApplicationScoped
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
     *          The page node to display. Normally, the implementation of PageNode
     *          is generated at compile time based on a Widget subclass that has
     *          been annotated with {@code @Page}. Anything calling this method
     *          must ensure that the given PageNode has been entered into the
     *          navigation graph, or later navigation back to {@code toPage} will
     *          fail.
     * @param state
     *          The state information to pass to the page node before showing it.
     */
    private Request(PageNode<W> pageNode, HistoryToken state) {
      this.pageNode = pageNode;
      this.state = state;
    }

  }

  private final SimplePanel contentPanel = new SimplePanel();

  private final NavigationGraph navGraph = GWT.create(NavigationGraph.class);

  protected PageNode<IsWidget> currentPage;

  private HandlerRegistration historyHandlerRegistration;

  /**
   * Indicates that a navigation request is currently processed.
   */
  private boolean locked = false;

  /**
   * Queued navigation requests which could not handled immediately.
   */
  private Queue<Request> queuedRequests = new LinkedList<Request>();

  private int redirectDepth = 0;

  @PostConstruct
  private void init() {
    if (navGraph.isEmpty()) return;

    historyHandlerRegistration = History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {

        HistoryToken token = HistoryToken.parse(event.getValue());
        PageNode<IsWidget> toPage = navGraph.getPage(token.getPageName());
        if (toPage == null) {
          GWT.log("Got invalid page name \"" + token.getPageName() + "\" in URL history token. Falling back to default page.");
          toPage = navGraph.getPage(""); // guaranteed at compile time to exist
        }
        navigate(new Request<IsWidget>(toPage, token));
      }
    });

    // finally, we bootstrap the navigation system (this invokes the callback above)
    History.fireCurrentHistoryState();
  }

  /**
   * Package private for testability.
   */
  @PreDestroy
  void cleanUp() {
    historyHandlerRegistration.removeHandler();
  }

  /**
   * Looks up the PageNode instance that provides content for the given widget
   * type, sets the state on that page, then makes the widget visible in the
   * content area.
   *
   * @param toPage
   *          The content type of the page node to look up and display.
   *          Normally, this is a Widget subclass that has been annotated with
   *          {@code @Page}.
   * @param state
   *          The state information to set on the page node before showing it.
   *          Normally the map keys correspond with the names of fields
   *          annotated with {@code @PageState} in the widget class, but this is
   *          not required.
   */
  public <W extends IsWidget> void goTo(Class<W> toPage, Multimap<String,String> state) {
    PageNode<W> toPageInstance = navGraph.getPage(toPage);
    navigate(toPageInstance, state);
  }

  /**
   * Same as {@link #goTo(Class, com.google.common.collect.Multimap)} but then with the page name.
   *
   * @param toPage the name of the page node to lookup and display.
   */
  public void goTo(String toPage) {
    PageNode<? extends IsWidget> toPageInstance = navGraph.getPage(toPage);
    navigate(toPageInstance);
  }

  /**
   * Looks up the PageNode instance of the page that has the unique role set and
   * makes the widget visible in the content area.
   *
   * @param role The unique role of the page that needs to be displayed.
   */
  public void goToWithRole(Class<? extends UniquePageRole> role) {
    PageNode<?> toPageInstance = navGraph.getPageByRole(role);
    navigate(toPageInstance);
  }

  /**
   * Return all PageNode instances that have specified pageRole.
   *
   * @param pageRole the role to find PageNodes by
   * @return All the pageNodes of the pages that have the specific pageRole.
   */
  public Collection<PageNode<? extends IsWidget>> getPagesByRole(Class<? extends PageRole> pageRole) {
    return navGraph.getPagesByRole(pageRole);
  }

  private <W extends IsWidget> void navigate(PageNode<W> toPageInstance) {
    navigate(toPageInstance, ImmutableListMultimap.<String, String>of());
  }

  private <W extends IsWidget> void navigate(PageNode<W> toPageInstance, Multimap<String, String> state) {
    HistoryToken token = HistoryToken.of(toPageInstance.name(), state);
    navigate(new Request<W>(toPageInstance, token));
  }

  /**
   * Captures a backup of the current page state in history, sets the state on
   * the given PageNode from the given state token, then makes its widget
   * visible in the content area.
   */
  private <W extends IsWidget> void navigate(Request<W> request) {
    if (locked) {
      queuedRequests.add(request);
      return;
    }

    redirectDepth++;
    if (redirectDepth >= MAXIMUM_REDIRECTS) {
      throw new RuntimeException(
              "Maximum redirect limit of " + MAXIMUM_REDIRECTS + " reached. " +
                      "Do you have a redirect loop?");
    }

    locked = true;
    try {
      hideCurrentPage();
      showPage(request.pageNode, request.state);
    } finally {
      locked = false;
    }

    if (queuedRequests.isEmpty()) {
      // No new navigation requests were recorded in the lifecycle methods.
      // This is the page which has to be displayed and the browser's history can be updated.
      redirectDepth = 0;
      History.newItem(request.state.toString(), false);
    } else {
      // Process all navigation requests captured in the lifecycle methods.
      while (queuedRequests.size() != 0) {
        navigate(queuedRequests.poll());
      }
    }

  }

  /**
   * Hide the page currently displayed and call the associated lifecycle methods.
   */
  private void hideCurrentPage() {
    IsWidget currentWidget = contentPanel.getWidget();

    // Note: Optimized out in production mode
    if (currentPage != null && currentWidget == null) {
      // This could happen if someone was manipulating the DOM behind our backs
      GWT.log("Current widget vanished from navigation content panel. " +
              "Not delivering pageHiding event to " + currentPage + ".");
    }

    if (currentPage != null && currentWidget != null) {
      currentPage.pageHiding(currentWidget);
    }

    // Ensure clean contentPanel regardless of currentPage being null
    contentPanel.clear();

    if (currentPage != null && currentWidget != null) {
      currentPage.pageHidden(currentWidget);
    }
  }

  /**
   * Show the given page and call the associated lifecycle methods.
   */
  private <W extends IsWidget> void showPage(final PageNode<W> toPage, final HistoryToken state) {
    toPage.produceContent(new CreationalCallback<W>() {
      @Override
      public void callback(W widget) {
        if (widget == null) {
          throw new NullPointerException("Target page " + toPage + " returned a null content widget");
        }

        toPage.pageShowing(widget, state);
        setCurrentPage(toPage);
        contentPanel.add(widget);
        toPage.pageShown(widget, state);
      }
    });
  }

  /**
   * Return the current page that is being displayed.
   * @return the current page
   */
  public PageNode<IsWidget> getCurrentPage() {
    return currentPage;
  }

  /**
   * Returns the panel that this Navigation object manages. The contents of this
   * panel will be updated by the navigation system in response to
   * PageTransition requests, as well as changes to the GWT navigation system.
   *
   * @return The content panel of this Navigation instance. It is not
   *         recommended that client code modifies the contents of this panel,
   *         because this Navigation instance may replace its contents at any
   *         time.
   */
  public IsWidget getContentPanel() {
    return contentPanel;
  }

  /**
   * Returns the navigation graph that provides PageNode instances to this Navigation instance.
   */
  // should this method be public? should we expose a way to set the nav graph?
  NavigationGraph getNavGraph() {
    return navGraph;
  }

  /**
   * Just sets the currentPage field. This method exists primarily to get around
   * a generics Catch-22.
   *
   * @param currentPage the new value for currentPage.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void setCurrentPage(PageNode currentPage) {
    this.currentPage = currentPage;
  }
}
