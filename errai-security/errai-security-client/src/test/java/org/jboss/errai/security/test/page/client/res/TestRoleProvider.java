package org.jboss.errai.security.test.page.client.res;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;

@Dependent
public class TestRoleProvider implements RequiredRolesProvider {

  @Override
  public Set<Role> getRoles() {
    return new HashSet<Role>(Arrays.asList(new RoleImpl("provided")));
  }

}
