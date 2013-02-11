package org.jboss.errai.ui.test.stylebinding.client.res;

import com.google.gwt.dom.client.Style;

import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton
public class StyleControl {
  private boolean admin = false;

  @AdminBinding
  private void adminStyleUpdate(Style style) {
    if (!admin) {
      style.setVisibility(Style.Visibility.HIDDEN);
    }
    else {
      style.setVisibility(Style.Visibility.VISIBLE);
    }
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }
}
