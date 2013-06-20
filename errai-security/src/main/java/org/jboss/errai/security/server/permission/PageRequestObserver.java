package org.jboss.errai.security.server.permission;

import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.NavigationEvent;
import org.jboss.errai.ui.nav.client.local.PageRequest;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
public class PageRequestObserver {

  private final AuthenticationService authenticationService;

  @Inject
  public PageRequestObserver(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  public void observer(@Observes NavigationEvent event) {
    final PageRequest pageRequest = event.getPageRequest();
    if (!authenticationService.hasPermission(pageRequest)) {
      throw new SecurityException("no enough rights to view this page!");
    }
  }
}
