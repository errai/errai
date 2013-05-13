package org.jboss.errai.demo.todo.server;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.RegistrationException;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.demo.todo.shared.User;

@Stateless @Service
public class SignupServiceImpl implements SignupService {

  @Inject EntityManager em;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public User register(User newUserObject) throws RegistrationException {
    em.persist(newUserObject);
    em.flush();
    System.out.println("Saved new user " + newUserObject + " (id=" + newUserObject.getId() + ")");
    return newUserObject;
  }

}
