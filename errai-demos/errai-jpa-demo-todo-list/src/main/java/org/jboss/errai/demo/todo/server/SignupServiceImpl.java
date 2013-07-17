package org.jboss.errai.demo.todo.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.RegistrationException;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.demo.todo.shared.User;
import org.jboss.errai.security.shared.AuthenticationService;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.SimpleUser;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.security.MessageDigest;

@Stateless @Service
public class SignupServiceImpl implements SignupService {

  @Inject AuthenticationService service;
  @Inject IdentityManager identityManager;
  @Inject EntityManager entityManager;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public User register(User newUserObject, String password) throws RegistrationException {
    final String email = newUserObject.getEmail().toLowerCase();
    SimpleUser user = new SimpleUser(email);
    user.setEmail(email);
    user.setFirstName("");
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
    service.login(newUserObject.getEmail(), password);
    return newUserObject;
  }
}
