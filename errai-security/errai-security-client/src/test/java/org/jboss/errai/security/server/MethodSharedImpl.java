package org.jboss.errai.security.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.client.shared.AdminMethodSharedImplService;
import org.jboss.errai.security.client.shared.UserMethodSharedImplService;

@Service
public class MethodSharedImpl implements UserMethodSharedImplService, AdminMethodSharedImplService {

  @Override
  public void someAdminService() {
  }

  @Override
  public void someUserService() {
  }

}
