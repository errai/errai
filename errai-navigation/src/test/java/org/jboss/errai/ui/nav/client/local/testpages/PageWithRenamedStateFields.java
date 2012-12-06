package org.jboss.errai.ui.nav.client.local.testpages;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;

import com.google.gwt.user.client.ui.FlexTable;

@ApplicationScoped @Page
public class PageWithRenamedStateFields extends FlexTable {

  @PageState("givenName") private String field;

  public String getField() {
    return field;
  }
}
