package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class RegistrationResult {

  private final User todoUser;

  private final org.jboss.errai.security.shared.User securityUser;

  public RegistrationResult(@MapsTo("todoUser") final User todoUser,
          @MapsTo("securityUser") final org.jboss.errai.security.shared.User securityUser) {
    this.todoUser = todoUser;
    this.securityUser = securityUser;
  }

  public User getTodoUser() {
    return todoUser;
  }

  public org.jboss.errai.security.shared.User getSecurityUser() {
    return securityUser;
  }

}
