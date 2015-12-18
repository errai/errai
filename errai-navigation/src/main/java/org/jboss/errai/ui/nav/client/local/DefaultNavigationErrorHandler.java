package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.ui.nav.client.local.api.PageNavigationErrorHandler;

import com.google.gwt.core.client.GWT;

/**
 * Implements default error handling behavior for page navigation.
 * 
 * @author Divya Dadlani <ddadlani@redhat.com>
 * 
 */
public class DefaultNavigationErrorHandler implements PageNavigationErrorHandler {

  private Navigation navigation;

  public DefaultNavigationErrorHandler(Navigation nav) {
    this.navigation = nav;
  }

  @Override
  public void handleInvalidPageNameError(Exception exception, String pageName) {
    if (pageName.equals("")) {
      throw new Error("Failed to initialize Default Page", exception);
    }
    else {
      GWT.log("Got invalid page name \"" + pageName + "\". Redirecting to default page.", exception);
      navigation.goTo("");
    }
  }

  @Override
  public void handleError(Exception exception, Class<? extends UniquePageRole> pageRole) {
    if (pageRole.equals(DefaultPage.class)) {
      throw new Error("Failed to initialize Default Page", exception);
    }
    else {
      GWT.log("Got invalid page role \"" + pageRole + "\". Redirecting to default page.", exception);
      navigation.goTo("");
    }
  }

  @Override
  public void handleInvalidURLError(Exception exception, String urlPath) {
    if(urlPath.equals("")) {
      throw new Error("Failed to initialize Default Page", exception);
    }
    else {
      GWT.log("Got invalid URL \"" + urlPath + "\". Redirecting to default page.", exception);
      navigation.goTo("");
    }
  }

}
