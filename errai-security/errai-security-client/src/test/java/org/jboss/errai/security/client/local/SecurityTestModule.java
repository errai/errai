package org.jboss.errai.security.client.local;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.client.local.identity.Identity;
import org.jboss.errai.security.shared.api.annotation.RestrictAccess;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.ui.shared.api.annotations.DataField;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author edewit@redhat.com
 */
@EntryPoint
public class SecurityTestModule extends Composite {
  @Inject
  Identity identity;

  @DataField
  @RestrictAccess(roles = "admin")
  Button test = new Button();

  void login(RemoteCallback<User> callback, BusErrorCallback errorCallback) {
    identity.login(callback, errorCallback);
  }
}
