package org.jboss.errai.security.demo.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.SecurityManager;

import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
@Service
public class MessageServiceImpl implements MessageService {
  @Inject
  SecurityManager securityManager;

  @Override
  @RequireAuthentication
  public String hello() {
    //User cannot be null becuase authentication is required for this method
    String name = securityManager.getUser().getFullName();
    return "Hello " + name + " how are you";
  }

  @Override
  public String ping() {
    return "pong";
  }
}
