package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Entry point into the Grocery List. This page's HTML template provides the
 * header and footer content that is present on every page of the app, and also
 * situates the navigation system's content panel into the main body of the
 * page. The navigation system takes responsibility for filling the content
 * panel with the appropriate body content based on the current history token in
 * the page URL.
 */
@Templated("#body")
@ApplicationScoped
@Bundle("GroceryListTranslation.json")
public class GroceryListClient extends Composite {

  @Inject
  private Navigation navigation;

  @Inject @DataField
  private NavBar navbar;

  @Inject @DataField
  private SimplePanel content;

  @Inject
  private Footer footer;

  @PostConstruct
  public void clientMain() {
    content.add(navigation.getContentPanel());
    RootPanel.get().add(this);
    RootPanel.get().add(footer);
  }
}
