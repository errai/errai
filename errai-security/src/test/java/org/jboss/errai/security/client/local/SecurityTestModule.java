package org.jboss.errai.security.client.local;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
@EntryPoint
public class SecurityTestModule extends Composite {
  @Inject
  Identity identity;

  @DataField
  @RequireRoles("admin")
  Button test = new Button();

  void login() {
    identity.login();
  }
}
