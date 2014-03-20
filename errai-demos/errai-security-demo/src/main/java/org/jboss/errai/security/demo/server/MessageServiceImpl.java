package org.jboss.errai.security.demo.server;

import javax.inject.Inject;

import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * @author edewit@redhat.com
 */
public class MessageServiceImpl implements MessageService {
  @Inject
  AuthenticationService authenticationService;

  @Override
  public String hello() {
    //User cannot be null because authentication is required for this method
    String name = authenticationService.getUser().getFullName();
    return "Hello " + name + " how are you";
  }

  @Override
  public String ping() {
    return "pong";
  }
}
