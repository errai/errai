package org.jboss.errai.demo.todo.client.local;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.ShadowService;
import org.jboss.errai.demo.todo.shared.RegistrationException;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.demo.todo.shared.User;

/**
 * @author edewit@redhat.com
 */
@ShadowService  @Service
public class SignupServiceShadow implements SignupService, MessageCallback {
  @Override
  public User register(User newUserObject, String password) throws RegistrationException {
    System.out.println("SignupServiceShadow.register");
    return null;
  }

  @Override
  public void callback(Message message) {
    System.out.println("SignupServiceShadow.callback");
  }
}
