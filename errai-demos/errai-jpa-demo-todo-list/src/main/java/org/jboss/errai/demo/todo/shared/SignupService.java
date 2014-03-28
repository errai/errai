package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface SignupService {

  /**
   * Attempts to register a new user in the system. If successful, the newly
   * registered user will be logged in on the server-side session.
   *
   * @param newUserObject
   *          The data about the user to register.
   * @return a copy of the newly registered User with its ID filled in.
   * @throws RegistrationException
   *           if the signup is not possible (for example, disallowed password;
   *           email address already in use; etc).
   */
  TodoListUser register(TodoListUser newUserObject, String password) throws RegistrationException;
}
