package org.jboss.errai.security.client.local.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.security.shared.api.identity.User;

@Dependent
public class BeanWithInjectedUser {

  @Inject
  private User user;
  
  public User getUser() {
    return user;
  }
}
