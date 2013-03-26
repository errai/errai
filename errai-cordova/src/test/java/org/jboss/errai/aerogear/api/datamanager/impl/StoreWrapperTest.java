package org.jboss.errai.aerogear.api.datamanager.impl;

import com.google.gwt.junit.client.GWTTestCase;
import org.jboss.errai.aerogear.api.datamanager.DataManager;
import org.jboss.errai.aerogear.api.datamanager.Store;

import java.util.Collection;

/**
 * @author edewit@redhat.com
 */
public class StoreWrapperTest extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.aerogear.api.datamanager.DataManager";
  }

  public void testStoreAndRetrieve() {
    //given
    DataManager dataManager = new DataManager();
    Store<User> store = dataManager.store();

    //when
    store.save(new User(1, "test"));
    store.save(new User(2, "test2"));
    store.save(new User(3, "test3"));
    store.save(new User(4, "test4"));

    //then
    Collection<User> collection = store.readAll();
    assertNotNull(collection);
    assertEquals(4, collection.size());
    assertTrue(collection.contains(new User(3)));

    User user = store.read(1);
    assertNotNull(user);
    assertEquals(new User(1), user);
  }

  public static class User {
    private int id;
    private String name;

    public User(int id) {
      this(id, null);
    }

    public User(int id, String name) {
      this.id = id;
      this.name = name;
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return "User{" +
              "id=" + id +
              ", name='" + name + '\'' +
              '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof User)) return false;

      User user = (User) o;

      if (id != user.id) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return id;
    }
  }
}
