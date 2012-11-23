package org.jboss.errai.ui.nav.test.client.local.testpages;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;

import com.google.gwt.user.client.ui.SimplePanel;

@ApplicationScoped
@Page
public class PageWithExtraState extends SimplePanel {

  @PageState
  private String stringThing;

  @PageState
  private int intThing;

  // TODO include fields for all the types we want to support

  public String getStringThing() {
    return stringThing;
  }

  public int getIntThing() {
    return intThing;
  }
}
