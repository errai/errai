package org.jboss.errai.jpa.test.client;


import java.sql.Date;

import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.jpa.test.entity.Album;
import org.jboss.errai.jpa.test.entity.Artist;

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
  public void testPersistOneAlbum() {

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
    assertEquals(album.toString(), fetchedAlbum.toString());
    assertEquals(album, fetchedAlbum);
  }

  /**
   * Tests the persistence of two unrelated entities of different types.
   */
  public void testPersistOneAlbumAndOneArtist() {

    // make Album (not attached to Artist)
    Album album = new Album();
    album.setId(10L); // same ID as album, on purpose
    album.setArtist(null);
    album.setName("Let It Be");
    album.setReleaseDate(new Date(11012400L));

    // make Artist (not attached to Album)
    Artist artist = new Artist();
    artist.setId(10L); // same ID as artist, on purpose
    artist.setName("The Beatles");

    // store them
    EntityManager em = JpaTestClient.INSTANCE.entityManager;
    em.persist(album);
    em.persist(artist);
    em.flush();
    em.detach(album);
    em.detach(artist);

    // fetch them
    Album fetchedAlbum = em.find(Album.class, album.getId());
    Artist fetchedArtist = em.find(Artist.class, artist.getId());
    assertNotSame(album, fetchedAlbum);
    assertNotSame(artist, fetchedArtist);

    // ensure Album is intact
    assertEquals(album.toString(), fetchedAlbum.toString());
    assertEquals(album, fetchedAlbum);

    // ensure Artist is intact
    assertEquals(artist.toString(), fetchedArtist.toString());
    assertEquals(artist, fetchedArtist);
  }

  /**
   * Tests that an entity in the "persistent" state (known to the entity manager
   * and not detached) is always a canonical reference to the same object.
   */
  public void testRetrievePersistentEntity() throws Exception {
    // make it
    Album album = new Album();
    album.setId(14L);
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    // store it
    EntityManager em = JpaTestClient.INSTANCE.entityManager;
    em.persist(album);

    // should come directly from the persistence unit cache at this point
    Album fetchedAlbum = em.find(Album.class, album.getId());
    assertSame(album, fetchedAlbum);

    // ensure it's stored in the database
    em.flush();

    // should still come directly from the persistence unit cache
    fetchedAlbum = em.find(Album.class, album.getId());
    assertSame(album, fetchedAlbum);
  }

}
