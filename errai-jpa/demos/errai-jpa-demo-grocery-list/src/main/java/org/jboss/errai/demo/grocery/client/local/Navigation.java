package org.jboss.errai.demo.grocery.client.local;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.errai.demo.grocery.client.local.nav.NavigationGraph;
import org.jboss.errai.demo.grocery.client.local.nav.Page;

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

  @Inject
  private NavigationGraph navGraph;

  /** TEMPORARY handcoded method, until a code generator provides the nav graph impl. */
  @Produces
  static NavigationGraph getNavigationGraph(Instance<WelcomePage> welcomePage, Instance<ItemListPage> itemListPage) {
    final Map<String, Instance<? extends Page>> pages = new HashMap<String, Instance<? extends Page>>();

    pages.put("", welcomePage); // this is the default page
    pages.put("WelcomePage", welcomePage);
    pages.put("ItemListPage", itemListPage);

    return new NavigationGraph() {
      @Override
      public Page getPage(String name) {
        return pages.get(name).get();
      }
    };
  }

  @PostConstruct
  private void init() {
    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        Page page = navGraph.getPage(event.getValue());
        makePageVisible(page);
      }
    });

    String initialToken = History.getToken();
    Page initialPage = navGraph.getPage(initialToken);
    if (initialPage == null) {
      initialPage = navGraph.getPage(""); // Default page
    }
    goTo(initialPage);
  }

  public void goTo(Page page) {
    makePageVisible(page);
    History.newItem(page.name(), false);
  }

  /**
   * Internal subroutine that makes the given page's widget visible but does not
   * manipulate the navigation history.
   *
   * @param page
   *          The page to show. Not null.
   */
  private void makePageVisible(Page page) {
    contentPanel.clear();
    contentPanel.add(page.content());
  }

  /**
   * Returns the panel that this Navigation object manages. The contents of this
   * panel will be updated by the navigation system in response to
   * PageTransition requests, as well as changes to the GWT navigation system.
   *
   * @return
   */
  public Widget getContentPanel() {
    return contentPanel;
  }

}
