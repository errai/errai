package org.jboss.errai.demo.todo.server;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.RegistrationException;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.demo.todo.shared.TodoListUser;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.User;
import org.slf4j.Logger;

@Stateless @Service
public class SignupServiceImpl implements SignupService {

  @Inject private IdentityManager identityManager;
  @Inject private EntityManager entityManager;
  @Inject private AuthenticationService authService;
  
  @Inject Logger logger;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public TodoListUser register(TodoListUser newTodoListUser, String password) throws RegistrationException {
    final User picketLinkUser = makePicketLinkUser(newTodoListUser);
    final String email = picketLinkUser.getEmail();

    //users login with their email address
    newTodoListUser.setLoginName(email);
    newTodoListUser.setEmail(email);

    identityManager.add(picketLinkUser);
    identityManager.updateCredential(picketLinkUser, new Password(password));

    entityManager.persist(newTodoListUser);
    entityManager.flush();
    entityManager.detach(newTodoListUser);

    logger.info("Saved new user " + newTodoListUser + " (id=" + newTodoListUser.getEmail() + ")");
    
    return (TodoListUser) authService.login(email, password);
  }
  
  private User makePicketLinkUser(TodoListUser todoListUser) {
    final String email = todoListUser.getEmail().toLowerCase();
    final User picketLinkUser = new User(email);

    picketLinkUser.setEmail(email);
    picketLinkUser.setFirstName(todoListUser.getShortName());
    picketLinkUser.setLastName(todoListUser.getFullName());
    
    return picketLinkUser;
  }

}
