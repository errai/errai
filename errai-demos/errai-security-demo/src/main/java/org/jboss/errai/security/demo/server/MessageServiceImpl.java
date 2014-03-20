package org.jboss.errai.security.demo.server;

import javax.inject.Inject;

import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.api.identity.User;
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
    final User user = authenticationService.getUser();
    String name = user.getFirstName() + " " + user.getLastName();
    return "Hello " + name + " how are you";
  }

  @Override
  public String ping() {
    return "pong";
  }
}
