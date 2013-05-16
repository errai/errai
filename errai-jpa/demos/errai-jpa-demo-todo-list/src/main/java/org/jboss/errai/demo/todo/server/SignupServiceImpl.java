package org.jboss.errai.demo.todo.server;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.AuthenticationException;
import org.jboss.errai.demo.todo.shared.LoginService;
import org.jboss.errai.demo.todo.shared.RegistrationException;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.demo.todo.shared.User;

@Stateless @Service
public class SignupServiceImpl implements SignupService {

  @Inject EntityManager em;
  @Inject LoginService loginService;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public User register(User newUserObject, String password) throws RegistrationException {
    newUserObject.setEmail(newUserObject.getEmail().toLowerCase());
    em.persist(newUserObject);
    em.flush();

    Query query = em.createNativeQuery(
            "UPDATE todolist_user SET password=:password WHERE id=:userId");
    query.setParameter("userId", newUserObject.getId());
    query.setParameter("password", LoginServiceImpl.toPasswordHash(newUserObject.getEmail(), password));
    query.executeUpdate();
    em.detach(newUserObject);

    System.out.println("Saved new user " + newUserObject + " (id=" + newUserObject.getId() + ")");
    try {
      loginService.logIn(newUserObject.getEmail(), password);
    } catch (AuthenticationException e) {
      throw new AssertionError(); // we just set the password to this inside the current transaction!
    }
    return newUserObject;
  }

}
