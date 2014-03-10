package org.jboss.errai.demo.todo.server;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.RegistrationException;
import org.jboss.errai.demo.todo.shared.RegistrationResult;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.security.shared.AuthenticationService;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.User;

@Stateless @Service
public class SignupServiceImpl implements SignupService {

  @Inject AuthenticationService service;
  @Inject IdentityManager identityManager;
  @Inject EntityManager entityManager;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public RegistrationResult register(org.jboss.errai.demo.todo.shared.User newUserObject, String password) throws RegistrationException {
    final String email = newUserObject.getEmail().toLowerCase();
    User user = new User(email);
    user.setEmail(email);
    user.setFirstName(newUserObject.getShortName());
    user.setLastName(newUserObject.getFullName());
    identityManager.add(user);
    identityManager.updateCredential(user, new Password(password));

    //users login with their email address
    newUserObject.setLoginName(email);
    newUserObject.setEmail(email);
    entityManager.persist(newUserObject);
    entityManager.flush();
    entityManager.detach(newUserObject);

    System.out.println("Saved new user " + newUserObject + " (id=" + newUserObject.getEmail() + ")");
    final org.jboss.errai.security.shared.User securityUser = service.login(newUserObject.getEmail(), password);
    
    return new RegistrationResult(newUserObject, securityUser);
  }
}
