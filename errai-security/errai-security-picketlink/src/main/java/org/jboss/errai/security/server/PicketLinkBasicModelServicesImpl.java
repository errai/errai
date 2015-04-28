package org.jboss.errai.security.server;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;

@Dependent
public class PicketLinkBasicModelServicesImpl implements PicketLinkBasicModelServices {
  @Inject
  private IdentityManager identityManager;

  @Inject
  private RelationshipManager relationshipManager;

  @Override
  public Role getRole(String name) throws IdentityManagementException {
    return BasicModel.getRole(identityManager, name);
  }

  @Override
  public boolean hasRole(IdentityType assignee, Role role) throws IdentityManagementException {
    return BasicModel.hasRole(relationshipManager, assignee, role);
  }

  @Override
  public boolean hasRole(IdentityType assignee, String roleName) throws IdentityManagementException {
    Role role = getRole(roleName);
    return role != null && BasicModel.hasRole(relationshipManager, assignee, role);
  }
}
