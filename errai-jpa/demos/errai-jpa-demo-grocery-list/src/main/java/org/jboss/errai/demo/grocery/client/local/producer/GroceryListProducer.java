package org.jboss.errai.demo.grocery.client.local.producer;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.grocery.client.shared.GroceryList;
import org.jboss.errai.demo.grocery.client.shared.User;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;

@ApplicationScoped
public class GroceryListProducer {

  @Produces @ApplicationScoped
  private GroceryList getGroceryList(final EntityManager em, final User user) {
    final TypedQuery<GroceryList> q = em.createNamedQuery("groceryListsForUser", GroceryList.class);
    q.setParameter("user", user);
    final List<GroceryList> groceryLists = q.getResultList();

    final GroceryList gl;
    if (groceryLists.isEmpty()) {
      gl = new GroceryList();
      gl.setOwner(user);
      em.persist(gl);
      em.flush();
    }
    else {
      gl = groceryLists.get(0);
    }

    return gl;
  }

}
