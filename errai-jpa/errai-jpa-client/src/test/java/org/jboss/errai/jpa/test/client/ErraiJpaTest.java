package org.jboss.errai.jpa.test.client;


import java.sql.Date;

import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.jpa.test.entity.Album;

import com.google.gwt.junit.client.GWTTestCase;

public class ErraiJpaTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().boostrapContainer();
  }

  public void testEntityManagerInjection() throws Exception {
    JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    assertNotNull(testClient.entityManager);
  }

  /**
   * Tests the rejection of a non-entity type.
   */
  public void testPersistNonEntity() {
    try {
      EntityManager em = JpaTestClient.INSTANCE.entityManager;
      em.persist("this is a string, not an entity");
      fail();
    } catch (IllegalArgumentException ex) {
      // this is the behaviour we are testing for
    }
  }

  /**
   * Tests the persistence of one entity with no related entities.
   */
  public void ignoretestPersistOneAlbum() { // implementation isn't complete yet

    // make it
    Album album = new Album();
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    // store it
    EntityManager em = JpaTestClient.INSTANCE.entityManager;
    em.persist(album);
    em.flush();
    em.detach(album);
    assertNotNull(album.getId());

    // fetch it
    Album fetchedAlbum = em.find(Album.class, album.getId());
    assertNotSame(album, fetchedAlbum);
  }
}
