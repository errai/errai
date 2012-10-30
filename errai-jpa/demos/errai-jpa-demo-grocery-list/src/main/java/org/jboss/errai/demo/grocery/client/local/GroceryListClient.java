package org.jboss.errai.demo.grocery.client.local;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.grocery.client.shared.GroceryList;
import org.jboss.errai.demo.grocery.client.shared.User;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Entry point into the Grocery List. This page's HTML template provides the
 * header and footer content that is present on every page of the app, and also
 * situates the navigation system's content panel into the main body of the
 * page. The navigation system takes responsibility for filling the content
 * panel with the appropriate body content based on the current history token in
 * the page URL.
 */
@Templated("#main")
@ApplicationScoped
@EntryPoint
public class GroceryListClient extends Composite {

  @Inject
  private Navigation navigation;

  @Inject @DataField
  private NavBar navbar;

  @Inject @DataField
  private SimplePanel content;

  @PostConstruct
  public void clientMain() {
    content.add(navigation.getContentPanel());
    RootPanel.get().add(this);
  }

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

  // same caveat as above
  @Produces @ApplicationScoped
  private GroceryList getGroceryList(final EntityManager em) {
    final User user = getUser(em);
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
