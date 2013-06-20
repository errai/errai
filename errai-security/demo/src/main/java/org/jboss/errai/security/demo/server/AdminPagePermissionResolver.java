package org.jboss.errai.security.demo.server;

import org.jboss.errai.security.server.permission.RequestPermissionResolver;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.local.PageRequest;

/**
 * @author edewit@redhat.com
 */
public class AdminPagePermissionResolver implements RequestPermissionResolver {

  @Override
  public PermissionStatus hasPermission(User user, PageRequest pageRequest) {
    if (user != null) {
      if ("hacker".equals(user.getLoginName())) {
        return PermissionStatus.ALLOW;
      }
      if ("WelcomePage".equals(pageRequest.getPageName()) && "john".equals(user.getLoginName())) {
        return PermissionStatus.DENY;
      }
    }
    return PermissionStatus.ALLOW;
  }
}
