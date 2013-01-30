package org.jboss.errai.demo.grocery.client.local.producer;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

import org.jboss.errai.demo.grocery.client.shared.User;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;

@ApplicationScoped
public class UserProducer {

  // not sure User (a JPA entity) should traditionally be an injectable CDI bean. We'll see how this pans out.
  @Produces @ApplicationScoped
  private User getUser(EntityManager em) {
    // XXX Of course, this only works if all the data is local.
    // When there is a server side to this demo, we will always have to authenticate with it before
    // we can produce a User instance capable of syncing.
    List<User> users = em.createNamedQuery("allUsers", User.class).getResultList();

    final User user;
    if (users.isEmpty()) {
      user = new User();
      user.setName("me");
      em.persist(user);
      em.flush();
    }
    else {
      user = users.get(0);
    }

    return user;
  }

}
