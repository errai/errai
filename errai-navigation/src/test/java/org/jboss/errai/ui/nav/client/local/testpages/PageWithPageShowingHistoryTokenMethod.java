package org.jboss.errai.ui.nav.client.local.testpages;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.HistoryToken;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageState;

import com.google.gwt.user.client.ui.VerticalPanel;

@ApplicationScoped @Page
public class PageWithPageShowingHistoryTokenMethod extends VerticalPanel {

  @PageState private String state;

  public int beforeShowCallCount = 0;

  public HistoryToken mostRecentStateToken;

  @PageShowing
  private void beforeShow(HistoryToken rawStateToken) {
    beforeShowCallCount++;
    mostRecentStateToken = rawStateToken;
  }
}
