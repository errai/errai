package org.jboss.errai.security.server.permission;

import org.jboss.errai.common.client.PageRequest;
import org.picketlink.permission.PermissionResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class PagePermissionResolver implements PermissionResolver {

  @Inject
  PermissionMapper permissionMapper;

  @Override
  public PermissionStatus hasPermission(Object resource, String operation) {
    if (resource instanceof PageRequest) {
      if (permissionMapper.resolvePermission((PageRequest) resource)) {
        return PermissionStatus.ALLOW;
      } else {
        return PermissionStatus.DENY;
      }
    }
    return PermissionStatus.NOT_APPLICABLE;
  }

  @Override
  public PermissionStatus hasPermission(Class<?> resourceClass, Serializable identifier, String operation) {
    return PermissionStatus.NOT_APPLICABLE;
  }
}
