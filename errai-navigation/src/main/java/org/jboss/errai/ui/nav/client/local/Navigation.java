package org.jboss.errai.ui.nav.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.common.collect.Multimap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

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

  private SimplePanel contentPanel = new SimplePanel();

  private NavigationGraph navGraph = GWT.create(NavigationGraph.class);

  protected PageNode<Widget> currentPage;

  @PostConstruct
  private void init() {
    if (navGraph.isEmpty()) return;

    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {

        HistoryToken token = HistoryToken.parse(event.getValue());
        PageNode<Widget> toPage = navGraph.getPage(token.getPageName());
        if (toPage == null) {
          GWT.log("Got invalid page name \"" + token.getPageName() + "\" in URL history token. Falling back to default page.");
          toPage = navGraph.getPage(""); // guaranteed at compile time to exist
        }
        show(toPage, token);

      }
    });

    // finally, we bootstrap the navigation system (this invokes the callback above)
    History.fireCurrentHistoryState();
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
  public <W extends Widget> void goTo(Class<W> toPage, Multimap<String,String> state) {
    PageNode<W> toPageInstance = navGraph.getPage(toPage);
    HistoryToken token = HistoryToken.of(toPageInstance.name(), state);
    show(toPageInstance, token);
    History.newItem(token.toString(), false);
  }

  /**
   * Captures a backup of the current page state in history, sets the state on
   * the given PageNode from the given state token, then makes its widget
   * visible in the content area.
   *
   * @param toPage
   *          The page node to display. Normally, the implementation of PageNode
   *          is generated at compile time based on a Widget subclass that has
   *          been annotated with {@code @Page}. Anything calling this method
   *          must ensure that the given PageNode has been entered into the
   *          navigation graph, or later navigation back to {@code toPage} will
   *          fail.
   * @param state
   *          The state information to pass to the page node before showing it.
   */
  private <W extends Widget> void show(PageNode<W> toPage, HistoryToken state) {

    if (currentPage != null) {
      Widget currentWidget = contentPanel.getWidget();
      if (currentWidget == null) {
        // this could happen if someone was manipulating the DOM behind our backs
        GWT.log("Current widget vanished from navigation content panel. " +
                "Not delivering pageHiding event to " + currentPage + ".");
      }
      else {
        currentPage.pageHiding(currentWidget);
      }
    }

    W widget = toPage.content();
    toPage.pageShowing(widget, state);

    contentPanel.clear();
    setCurrentPage(toPage);
    contentPanel.add(widget);
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
  public Widget getContentPanel() {
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
