package org.jboss.errai.ui.nav.client.local.testpages;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageHiding;
import org.jboss.errai.ui.nav.client.local.api.NavigationControl;

import com.google.gwt.user.client.ui.SimplePanel;

@Page
@ApplicationScoped
public class PageWithNavigationControl extends SimplePanel {
  
  public NavigationControl control;
  
  @PageHiding
  private void confirm(final NavigationControl control) {
    this.control = control;
  }

}
