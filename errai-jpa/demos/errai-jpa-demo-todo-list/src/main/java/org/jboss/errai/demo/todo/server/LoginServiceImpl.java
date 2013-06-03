package org.jboss.errai.demo.todo.server;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.AuthenticationException;
import org.jboss.errai.demo.todo.shared.LoginService;
import org.jboss.errai.demo.todo.shared.User;

@SessionScoped @Service
public class LoginServiceImpl implements LoginService, Serializable {

  @Inject EntityManager em;

  private User currentUser;

  public LoginServiceImpl() {
    System.out.println("New LoginServiceImpl created: " + System.identityHashCode(this));
  }

  @Override
  public User logIn(String email, String password) throws AuthenticationException {
    Query query = em.createNativeQuery(
            "SELECT id, email, fullname, shortname FROM todolist_user WHERE email=:email AND password=:password", User.class);
    query.setParameter("email", email.toLowerCase());
    query.setParameter("password", toPasswordHash(email.toLowerCase(), password));
    List<?> resultList = query.getResultList();
    if (resultList.size() != 1) {
      throw new AuthenticationException();
    }
    currentUser = (User) resultList.get(0);
    System.out.println("LoginServiceImpl@" + System.identityHashCode(this) + " setting current user " + currentUser);
    return currentUser;
  }

  @Override
  public User whoAmI() {
    System.out.println("LoginServiceImpl@" + System.identityHashCode(this) + " returning current user " + currentUser);
    return currentUser;
  }

  @Override
  public void logOut() {
    currentUser = null;
  }

  public static String toPasswordHash(String username, String password) {
    try {
      MessageDigest sha1 = MessageDigest.getInstance("sha-1");
      sha1.update(username.getBytes("utf-8"));
      byte[] hash = sha1.digest(password.getBytes("utf-8"));
      StringBuilder sb = new StringBuilder(128);
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
