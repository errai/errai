package org.jboss.errai.demo.todo.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.ShareList;
import org.jboss.errai.demo.todo.shared.ShareService;
import org.jboss.errai.demo.todo.shared.UnknownUserException;
import org.jboss.errai.demo.todo.shared.User;
import org.jboss.errai.security.shared.AuthenticationService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * @author edewit@redhat.com
 */
@Stateless @Service
public class ShareServiceImpl implements ShareService {

  @Inject AuthenticationService service;
  @Inject EntityManager entityManager;

  @Override
  public void share(String email) throws UnknownUserException {
    org.jboss.errai.security.shared.User currentUser = service.getUser();

    //if this was the real world we would sent a mail to the user that this todo list was just shared with him.
    //but this is _only_ a demo.

    final TypedQuery<User> query = entityManager.createNamedQuery("userByEmail", User.class);
    query.setParameter("email", email);
    User user;
    try {
      user = query.getSingleResult();
    } catch (NoResultException exception) {
      throw new UnknownUserException("user with email '" + email + "' is not a registered user");
    }

    ShareList shareList = entityManager.find(ShareList.class, currentUser.getLoginName());
    if (shareList == null) {
      shareList = new ShareList();
      shareList.setLoginName(currentUser.getLoginName());
    }

    shareList.getSharedWith().add(user);

    entityManager.persist(shareList);
    entityManager.flush();
  }
}
