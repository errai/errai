package org.jboss.errai.security.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.client.shared.AdminTypeSharedImplService;
import org.jboss.errai.security.client.shared.UserTypeSharedImplService;

@Service
public class TypeSharedImpl implements AdminTypeSharedImplService, UserTypeSharedImplService {

  @Override
  public void someUserService() {
  }

  @Override
  public void someAdminService() {
  }

}
