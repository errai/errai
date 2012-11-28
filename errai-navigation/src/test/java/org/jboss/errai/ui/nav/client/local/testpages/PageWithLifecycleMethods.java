package org.jboss.errai.ui.nav.client.local.testpages;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageHiding;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageState;

import com.google.gwt.user.client.ui.VerticalPanel;

@ApplicationScoped @Page
public class PageWithLifecycleMethods extends VerticalPanel {

  @PageState private String state;

  public int beforeShowCallCount = 0;
  public int beforeHideCallCount = 0;

  public String stateWhenBeforeShowWasCalled;

  @PageShowing
  private void beforeShow() {
    beforeShowCallCount++;
    stateWhenBeforeShowWasCalled = state;
  }

  @PageHiding
  private void beforeHide() {
    beforeHideCallCount++;
    state = "lastMinuteChange";
  }
}
