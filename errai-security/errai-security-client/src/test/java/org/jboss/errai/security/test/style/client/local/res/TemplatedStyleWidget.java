package org.jboss.errai.security.test.style.client.local.res;

import javax.inject.Inject;

import org.jboss.errai.security.shared.api.annotation.RestrictAccess;
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
  @RestrictAccess(roles = "user")
  private Anchor userAnchor;
  
  @Inject
  @DataField
  @RestrictAccess(roles = "admin")
  private Anchor adminAnchor;

  @Inject
  @DataField
  @RestrictAccess(roles = {"user", "admin"})
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
