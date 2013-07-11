package org.jboss.errai.security.client.local;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.SecurityError;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.shared.NavigationEvent;
import org.jboss.errai.ui.nav.client.shared.PageRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class PageRequestObserver {

  @Inject
  Navigation navigation;

  @Inject
  private Caller<AuthenticationService> authenticationService;

  public void observer(@Observes NavigationEvent event) {
    final PageRequest pageRequest = event.getPageRequest();
    try {
      authenticationService.call(new RemoteCallback<Boolean>() {
        @Override
        public void callback(Boolean response) {
          if (!response) {
            navigation.goToWithRole(SecurityError.class);
          }
        }
      }).hasPermission(pageRequest);
    } catch (Exception e) {
      //we get a navigation even on the first page when the system is not ready.
    }
  }
}
