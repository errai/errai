package org.jboss.errai.ui.nav.client.local.testpages;

import org.jboss.errai.ui.nav.client.local.PageHiding;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageState;

import com.google.gwt.user.client.ui.HorizontalPanel;

public class BasePageForTestingInheritance extends HorizontalPanel {

  private @PageState String inheritedState;

  public int beforePageShowCallCount = 0;
  public int beforePageHideCallCount = 0;

  public String stateWhenBeforeShowWasCalled;

  @PageShowing
  protected void beforeShow() {
    beforePageShowCallCount++;
    stateWhenBeforeShowWasCalled = inheritedState;
  }

  @PageHiding
  protected void beforeHide() {
    beforePageHideCallCount++;
  }

  public String getMyState() {
    return inheritedState;
  }
}
