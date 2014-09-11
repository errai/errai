package org.jboss.errai.ui.nav.client.local.testpages;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageState;

import com.google.gwt.user.client.ui.SimplePanel;

@Dependent
@Page(path="page_b_with_state")
public class PageBWithState extends SimplePanel {

  @PageState
  public String uuid;
  
  public static int hitCount;

  /**
   * Constructor.
   */
  public PageBWithState() {
  }

  @PageShowing
  public void onPageShowing() {
    hitCount++;
  }
}
