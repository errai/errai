package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.ui.nav.client.local.api.PageNavigationErrorHandler;

import com.google.gwt.core.client.GWT;

/**
 * Implements default error handling behavior for page navigation.
 * 
 * @author Divya Dadlani <ddadlani@redhat.com>
 * 
 */
public class DefaultNavigationErrorHandler implements
        PageNavigationErrorHandler {

  private Navigation navigation;

  public DefaultNavigationErrorHandler(Navigation navigation) {
    this.navigation = navigation;
  }

  @Override
  public void handleError(Exception exception, String pageName) {

    GWT.log("Got invalid page name \"" + pageName
            + "\". Falling back to default page.", exception);
    navigation.goTo("");// guaranteed at compile time to exist

  }

  @Override
  public void handleError(Exception exception,
          Class<? extends PageRole> pageRole) {
    GWT.log("Got invalid page role \"" + pageRole
            + "\". Falling back to default page.", exception);
    navigation.goTo("");// guaranteed at compile time to exist
  }

}
