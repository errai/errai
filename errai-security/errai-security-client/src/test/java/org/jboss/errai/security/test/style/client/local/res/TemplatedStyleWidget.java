package org.jboss.errai.security.test.style.client.local.res;

import javax.inject.Inject;

import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

@Templated
public class TemplatedStyleWidget extends Composite {
  
  @Inject
  @DataField
  private Anchor control;
  
  @Inject
  @DataField
  @RestrictedAccess(roles = "user")
  private Anchor userAnchor;
  
  @Inject
  @DataField
  @RestrictedAccess(roles = "admin")
  private Anchor adminAnchor;

  @Inject
  @DataField
  @RestrictedAccess(roles = {"user", "admin"})
  private Anchor userAdminAnchor;

  public Anchor getUserAnchor() {
    return userAnchor;
  }

  public Anchor getAdminAnchor() {
    return adminAnchor;
  }

  public Anchor getUserAdminAnchor() {
    return userAdminAnchor;
  }

  public Anchor getControl() {
    return control;
  }

}
