package org.jboss.errai.security.client.local;

import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
@Templated("App.html#root")
public class SecurityTestModule extends Composite {
  @Inject
  Identity identity;

  void login() {
    identity.login();
  }
}
