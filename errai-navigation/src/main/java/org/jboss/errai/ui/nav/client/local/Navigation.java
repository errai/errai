package org.jboss.errai.ui.nav.client.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        // TODO what about backing up current history? we might already be "back" in the nav stack already

        List<String> tokens = parseHistoryToken(event.getValue());
        PageNode toPage = navGraph.getPage(tokens.get(0));
        Widget widget = toPage.content();
        toPage.putState(widget, tokens.subList(1, tokens.size()));

        contentPanel.clear();
        contentPanel.add(widget);
      }
    });

    List<String> initialTokens = parseHistoryToken(History.getToken());
    PageNode initialPage = navGraph.getPage(initialTokens.get(0));
    if (initialPage == null) {
      initialPage = navGraph.getPage(""); // Default page
    }
    goTo(initialPage, initialTokens.subList(1, initialTokens.size()).toArray(new String[initialTokens.size() - 1]));
  }

  /**
   * Goes to
   * @param toPage
   */
  public void goTo(Class<? extends Widget> toPage, String ... state) {
    PageNode toPageInstance = navGraph.getPage(toPage);
    goTo(toPageInstance, state);
  }

  public void goTo(PageNode toPage, String ... state) {

    // TODO preserve state of current page

    Widget widget = toPage.content();
    toPage.putState(widget, Arrays.asList(state));

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
   * Breaks up the given history token at all unescaped '\' characters.
   *
   * @param token
   *          The history token to parse. Must be non-null.
   * @return A list of size >= 1. The first entry is always the page name.
   *         Remaining entries are extra state info.
   */
  private static List<String> parseHistoryToken(String token) {
    StringBuilder nextPart = new StringBuilder();
    List<String> parts = new ArrayList<String>();
    for (int i = 0; i < token.length(); i++) {
      char ch = token.charAt(i);
      if (ch == '\\') {
        nextPart.append(token.charAt(i + 1));
        i++;
      }
      else if (ch == '/') {
        parts.add(nextPart.toString());
        nextPart = new StringBuilder();
      }
      else {
        nextPart.append(ch);
      }
    }
    parts.add(nextPart.toString());
    return parts;
  }

  private static String makeHistoryToken(String pageName, List<String> extraInfo) {
    StringBuilder sb = new StringBuilder();
    appendEscaped(sb, pageName);
    for (String state : extraInfo) {
      sb.append("/");
      appendEscaped(sb, state);
    }
    return sb.toString();
  }

  private static void appendEscaped(StringBuilder sb, String state) {
    for (int i = 0; i < state.length(); i++) {
      char ch = state.charAt(i);
      if (ch == '/') {
        sb.append('\\');
      }
      sb.append(ch);
    }
  }

}
