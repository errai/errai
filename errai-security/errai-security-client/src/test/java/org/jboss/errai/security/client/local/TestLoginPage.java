package org.jboss.errai.security.client.local;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;

import com.google.gwt.user.client.ui.SimplePanel;

@Page(role = LoginPage.class)
@ApplicationScoped
public class TestLoginPage extends SimplePanel {
  
  private int pageLoadCounter = 0;

  public int getPageLoadCounter() {
    return pageLoadCounter;
  }

  @PageShowing
  public void incrementCounter() {
    pageLoadCounter += 1;
  }

}
