package org.jboss.errai.security.server.permission;

import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.local.PageRequest;
import org.junit.Before;
import org.junit.Test;

import javax.enterprise.inject.Instance;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author edewit@redhat.com
 */
public class PermissionMapperTest {
  private List<RequestPermissionResolver> resolverList = Arrays.asList(new PermissionResolver(), new PermissionStatus2());
  private PermissionMapper permissionMapper;

  @Before
  public void setup() {
    // given
    permissionMapper = new PermissionMapper();
    permissionMapper.service = mock(AuthenticationService.class);
    permissionMapper.resolvers = mock(Instance.class);
  }

  @Test
  public void shouldTestPermissionResolvers() {

    // when
    when(permissionMapper.resolvers.iterator()).thenReturn(resolverList.iterator());
    when(permissionMapper.service.getUser()).thenReturn(new User("john"));
    final boolean allowed = permissionMapper.resolvePermission(new PageRequest("admin", null));

    // then
    assertFalse("When the user is john nothing should be allowed", allowed);
  }

  @Test
  public void shouldTestPermissionResolving() {

    // when
    when(permissionMapper.resolvers.iterator()).thenReturn(resolverList.iterator());
    when(permissionMapper.service.getUser()).thenReturn(new User("edewit"));
    final boolean allowed = permissionMapper.resolvePermission(new PageRequest("index", null));

    // then
    assertTrue("index should have been allowed", allowed);
  }

  @Test
  public void shouldTestPermissionResolving2() {

    // when
    when(permissionMapper.resolvers.iterator()).thenReturn(resolverList.iterator());
    when(permissionMapper.service.getUser()).thenReturn(new User("john"));
    final boolean allowed = permissionMapper.resolvePermission(new PageRequest("index", null));

    // then
    assertFalse("john is never allowed", allowed);
  }

  public class PermissionResolver implements RequestPermissionResolver {
    @Override
    public PermissionStatus hasPermission(User user, PageRequest pageRequest) {
      if ("john".equals(user.getLoginName())) {
        return PermissionStatus.DENY;
      }
      return PermissionStatus.NOT_APPLICABLE;
    }
  }

  public class PermissionStatus2 implements RequestPermissionResolver {

    @Override
    public PermissionStatus hasPermission(User user, PageRequest pageRequest) {
      if ("index".equals(pageRequest.getPageName())) {
        return PermissionStatus.ALLOW;
      }
      if ("admin".equals(pageRequest.getPageName())) {
        return PermissionStatus.DENY;
      }
      return PermissionStatus.NOT_APPLICABLE;
    }
  }
}
