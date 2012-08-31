package org.jboss.errai.ui.nav.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

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

  @SuppressWarnings("unused")
  @Inject
  private IOCBeanManager bm;

  private NavigationGraph navGraph = GWT.create(NavigationGraph.class);

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        PageNode page = navGraph.getPage(event.getValue());
        makePageVisible(page);
      }
    });

    String initialToken = History.getToken();
    PageNode initialPage = navGraph.getPage(initialToken);
    if (initialPage == null) {
      initialPage = navGraph.getPage(""); // Default page
    }
    goTo(initialPage);
  }

  /**
   * Goes to
   * @param toPage
   */
  public void goTo(Class<? extends Widget> toPage) {
    PageNode toPageInstance = navGraph.getPage(toPage);
    goTo(toPageInstance);
  }

  public void goTo(PageNode toPage) {
    makePageVisible(toPage);
    History.newItem(toPage.name(), false);
  }

  /**
   * Internal subroutine that makes the given page's widget visible but does not
   * manipulate the navigation history.
   *
   * @param page
   *          The page to show. Not null.
   */
  private void makePageVisible(PageNode page) {
    contentPanel.clear();
    contentPanel.add(page.content());
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

}
