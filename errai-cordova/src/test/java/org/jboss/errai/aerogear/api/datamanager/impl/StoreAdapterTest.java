package org.jboss.errai.aerogear.api.datamanager.impl;

import com.google.gwt.junit.client.GWTTestCase;
import org.jboss.errai.aerogear.api.datamanager.DataManager;
import org.jboss.errai.aerogear.api.datamanager.Store;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Collection;

/**
 * @author edewit@redhat.com
 */
public class StoreAdapterTest extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.aerogear.api.AerogearTests";
  }

  public void testStoreAndRetrieve() {
    //given
    DataManager dataManager = new DataManager();
    Store<User> store = dataManager.store(User.class);

    //when
    store.save(new User(1L, "test"));
    store.save(new User(2L, "test2"));
    store.save(new User(3L, "test3"));
    store.save(new User(4L, "test4"));

    //then
    Collection<User> collection = store.readAll();
    assertNotNull(collection);
    assertEquals(4, collection.size());
    assertTrue(collection.contains(new User(3)));

    User user = store.read(2);
    assertNotNull(user);
    assertEquals(new User(2), user);
    assertEquals("test2", user.name);

    store.remove(3);
    collection = store.readAll();
    assertEquals(3, collection.size());
  }

  @Portable
  public static class User {
    private Long id;
    private String name;

    public User(long id) {
      this(id, null);
    }

    public User(@MapsTo("id") Long id, @MapsTo("name") String name) {
      this.id = id;
      this.name = name;
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
      return !(id != null ? !id.equals(user.id) : user.id != null);
    }

    @Override
    public int hashCode() {
      return id != null ? id.hashCode() : 0;
    }
  }
}
