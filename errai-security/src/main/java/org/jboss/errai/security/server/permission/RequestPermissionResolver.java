package org.jboss.errai.security.server.permission;

import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.local.PageRequest;

/**
 * @author edewit@redhat.com
 */
public interface RequestPermissionResolver {

  public enum PermissionStatus {
    ALLOW, DENY, NOT_APPLICABLE
  }

  /**
   * Tests if the currently authenticated user has permission to 'see' the specified page request.
   *
   * @param user the user to validate the pageRequest for
   * @param pageRequest The pageRequest for which the permission is required
   * @return ALLOW if the current user has the permission DENY or NOT_APPLICABLE.
   */
  PermissionStatus hasPermission(User user, PageRequest pageRequest);

}
