package org.jboss.errai.ui.nav.client.local.res;

import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.nav.client.local.api.PageNavigationErrorHandler;

public class TestNavigationErrorHandler implements PageNavigationErrorHandler {
  public int count;

  @Override
  public void handleInvalidPageNameError(Exception exception, String pageName) {
    count++;
  }

  @Override
  public void handleError(Exception exception, Class<? extends UniquePageRole> pageRole) {
    handleInvalidPageNameError(exception, "");

  }

  @Override
  public void handleInvalidURLError(Exception exception, String urlPath) {
    count++;    
  }

}
