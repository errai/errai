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
import com.google.gwt.user.client.ui.Panel;
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

  private Panel contentPanel = new SimplePanel();

  private NavigationGraph navGraph = GWT.create(NavigationGraph.class);

  @PostConstruct
  private void init() {
    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {

        // TODO what about backing up current history? we might already be "back" in the nav stack already

        HistoryToken token = HistoryToken.parse(event.getValue());
        PageNode<Widget> toPage = navGraph.getPage(token.getPageName());
        Widget widget = toPage.content();
        toPage.putState(widget, token.getState());

        contentPanel.clear();
        contentPanel.add(widget);
      }
    });

    HistoryToken initialToken = HistoryToken.parse(History.getToken());
    PageNode<Widget> initialPage = navGraph.getPage(initialToken.getPageName());
    if (initialPage == null) {
      initialPage = navGraph.getPage(""); // Default page
    }
    goTo(initialPage, initialToken.getState());
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
   *          annotated with {@code @PageState} in the widget class.
   */
  public <W extends Widget> void goTo(Class<W> toPage, Multimap<String,String> state) {
    PageNode<W> toPageInstance = navGraph.getPage(toPage);
    goTo(toPageInstance, state);
  }

  /**
   * Sets the state on the given PageNode, then makes its widget visible in the
   * content area.
   *
   * @param toPage
   *          The page node to display. Normally, the implementation of PageNode
   *          is generated at compile time based on a Widget subclass that has
   *          been annotated with {@code @Page}. Anything calling this method
   *          must ensure that the given PageNode has been entered into the
   *          navigation graph, or later navigation back to {@code toPage} will
   *          fail.
   * @param state
   *          The state information to set on the page node before showing it.
   *          Normally the map keys correspond with the names of fields
   *          annotated with {@code @PageState} in the widget class.
   */
  private <W extends Widget> void goTo(PageNode<W> toPage, Multimap<String,String> state) {

    // TODO preserve state of current page

    W widget = toPage.content();
    toPage.putState(widget, state);

    contentPanel.clear();
    contentPanel.add(widget);
    History.newItem(toPage.name(), false);
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
}
