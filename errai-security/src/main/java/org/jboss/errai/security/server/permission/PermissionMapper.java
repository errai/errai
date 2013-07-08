package org.jboss.errai.security.server.permission;

import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.local.PageRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static org.jboss.errai.security.server.permission.RequestPermissionResolver.PermissionStatus;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class PermissionMapper {
  @Inject
  Instance<RequestPermissionResolver> resolvers;

  @Inject
  AuthenticationService service;

  public boolean resolvePermission(PageRequest pageRequest) {
    User user = service.getUser();

    for (RequestPermissionResolver resolver : resolvers) {
      PermissionStatus status = resolver.hasPermission(user, pageRequest);
    if (PermissionStatus.DENY.equals(status)) {
        return false;
      }
    }

    return true;
  }
}