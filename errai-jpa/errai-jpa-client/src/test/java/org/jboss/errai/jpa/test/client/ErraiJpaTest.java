package org.jboss.errai.jpa.test.client;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.jboss.errai.common.client.api.WrappedPortable;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.jpa.rebind.ErraiEntityManagerGenerator;
import org.jboss.errai.jpa.test.entity.Album;
import org.jboss.errai.jpa.test.entity.Artist;
import org.jboss.errai.jpa.test.entity.CallbackLogEntry;
import org.jboss.errai.jpa.test.entity.CascadeFrom;
import org.jboss.errai.jpa.test.entity.Format;
import org.jboss.errai.jpa.test.entity.Genre;
import org.jboss.errai.jpa.test.entity.StandaloneLifecycleListener;
import org.jboss.errai.jpa.test.entity.Zentity;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Tests the JPA EntityManager facilities provided by Errai JPA.
 * <p>
 * Note that there is a {@link HibernateJpaTest subclass of this test} that runs
 * all the same checks against Hibernate, as a sanity check that we're testing
 * for actual JPA-sanctioned and JPA-compatible behaviour.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class ErraiJpaTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  protected EntityManager getEntityManager() {
    JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    assertNotNull(testClient.entityManager);
    return JpaTestClient.INSTANCE.entityManager;
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    Album.CALLBACK_LOG.clear();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().bootstrapContainer();
  }

  /**
   * Tests that the entity manager was injected into the testing class. If this
   * test fails, the likely cause is that the
   * {@link ErraiEntityManagerGenerator} failed to output a compilable class. In
   * that case, try re-running this test with
   * {@code -Derrai.codegen.permissive=true} and
   * {@code -Derrai.codegen.printOut=true}. This should allow you to inspect the
   * generated source code and to see the Java compiler errors.
   */
  public void testEntityManagerInjection() throws Exception {
    getEntityManager(); // has its own assertions
  }

  /**
   * Tests the rejection of a non-entity type.
   */
  public void testPersistNonEntity() {
    try {
      EntityManager em = getEntityManager();
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
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();
    em.detach(album);
    assertNotNull(album.getId());

    // fetch it
    Album fetchedAlbum = em.find(Album.class, album.getId());
    assertNotSame(album, fetchedAlbum);
    assertEquals(album.toString(), fetchedAlbum.toString());
  }

  /**
   * Tests the persistence of two unrelated entities of different types.
   */
  public void testPersistOneAlbumAndOneArtist() {

    // make Album (not attached to Artist)
    Album album = new Album();
    album.setArtist(null);
    album.setName("Let It Be");
    album.setReleaseDate(new Date(11012400000L));

    // store them
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();

    // make Artist (completely unrelated to Album, but has same numeric ID)
    Artist artist = new Artist();
    artist.setId(album.getId()); // to verify proper separation by entity type
    artist.setName("The Beatles");

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

    // ensure Artist is intact
    assertEquals(artist.toString(), fetchedArtist.toString());
  }

  /**
   * Tests that an entity that was just persisted in this session is always a
   * canonical reference to the same object.
   */
  public void testRetrievePersistedEntity() throws Exception {
    // make it
    Album album = new Album();
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    // store it
    EntityManager em = getEntityManager();
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

  /**
   * Tests that an entity that was fetched in this session is always a canonical
   * reference to the same object.
   */
  public void testRetrieveEntityTwice() throws Exception {
    // make it
    Album album = new Album();
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);

    // ensure it's stored in the database
    em.flush();

    // now forget it
    em.detach(album);

    // multiple fetches should come directly from the persistence unit cache at this point
    Album fetchedAlbum = em.find(Album.class, album.getId());
    Album fetchedAlbum2 = em.find(Album.class, album.getId());
    assertSame(fetchedAlbum, fetchedAlbum2);

    // ensure it's not the original instance we persisted and detached
    assertNotSame(album, fetchedAlbum);
  }

  /**
   * Tests the persistence of two unrelated entities of different types.
   */
  public void testClearDetachesAll() {

    // make Album
    Album album = new Album();
    album.setArtist(null);
    album.setName("Let It Be");
    album.setReleaseDate(new Date(11012400000L));

    // make artist
    Artist artist = new Artist();
    artist.setId(123L);
    artist.setName("The Beatles");

    // store them
    EntityManager em = getEntityManager();
    em.persist(album);
    em.persist(artist);
    em.flush();

    // make sure they're persistent and managed
    assertSame(album, em.find(Album.class, album.getId()));
    assertSame(artist, em.find(Artist.class, artist.getId()));

    em.clear();

    // make sure they were detached
    Album fetchedAlbum = em.find(Album.class, album.getId());
    Artist fetchedArtist = em.find(Artist.class, artist.getId());
    assertNotNull(fetchedAlbum);
    assertNotNull(fetchedArtist);
    assertNotSame(album, fetchedAlbum);
    assertNotSame(artist, fetchedArtist);
  }

  /**
   * Tests the persistence of two unrelated entities of different types.
   */
  public void testRemoveOneEntity() {

    // make Album
    Album album = new Album();
    album.setArtist(null);
    album.setName("Let It Be");
    album.setReleaseDate(new Date(11012400000L));

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();

    // make sure it's persistent and managed
    assertSame(album, em.find(Album.class, album.getId()));

    // remove it
    em.remove(album);

    // make sure it's gone
    assertNotNull(album.getId());
    assertNull(em.find(Album.class, album.getId()));
  }

  public void testUpdateEntity() throws Exception {

    // make it
    Album album = new Album();
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();

    // modify it
    album.setName("Cowabunga");
    em.flush();

    // fetch and compare
    em.clear();
    Album fetchedAlbum = em.find(Album.class, album.getId());
    assertNotNull(fetchedAlbum);
    assertNotSame(album, fetchedAlbum);
    assertEquals(album.toString(), fetchedAlbum.toString());
  }

  public void testIdUpdateIsRejected() throws Exception {

    // make it
    Album album = new Album();
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();

    // set ID (not allowed)
    try {
      album.setId(1234L);
      em.flush();
      fail("ID change was not detected");
    } catch (PersistenceException e) {
      assertTrue(
              e.getMessage().contains("Actual ID: 1234") || // errai message
              e.getMessage().contains("to 1234")); // hibernate message
    }
  }

  public void testPersistRelatedCollection() {
    // make them
    Artist artist = new Artist();
    artist.setId(9L); // Artist uses user-assigned/non-generated IDs
    artist.setName("The Beatles");

    Album album = new Album();
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    album.setArtist(artist);
    artist.addAlbum(album);

    // store it
    EntityManager em = getEntityManager();
    em.persist(artist); // should cascade onto album, which is in the collection relation
    em.flush();

    assertNotNull(album.getId());

    // ensure both are in the persistence context
    assertSame(artist, em.find(Artist.class, artist.getId()));
    assertSame(album, em.find(Album.class, album.getId()));

    em.clear();

    // ensure both are retrieved (TBD: should Errai always/ever fetch related entities?)
    Artist fetchedArtist = em.find(Artist.class, artist.getId());
    assertNotNull(fetchedArtist);

    assertEquals(1, fetchedArtist.getAlbums().size());
    Album cascadeFetchedAlbum = fetchedArtist.getAlbums().iterator().next();
    assertNotNull(cascadeFetchedAlbum);

    assertNotSame(artist, fetchedArtist);
    assertNotSame(album, cascadeFetchedAlbum);

    assertEquals(artist.toString(), fetchedArtist.toString());
    assertEquals(album.toString(), cascadeFetchedAlbum.toString());

    // now ensure we haven't retrieved a ghost album
    assertSame(cascadeFetchedAlbum, em.find(Album.class, cascadeFetchedAlbum.getId()));

    // ensure "parent pointer" of album points at correct artist instance
    assertSame(fetchedArtist, cascadeFetchedAlbum.getArtist());
  }

  public void testPersistNullOneToMany() {
    // artist is the container
    Artist artist = new Artist();
    artist.setId(98L); // Artist uses user-assigned/non-generated IDs
    artist.setName("The Beatles");

    // this one has the null artist
    Album album = new Album();
    album.setName("Mystery Album");
    album.setArtist(null);
    album.setReleaseDate(new Date(-9366400000L));

    // store it
    EntityManager em = getEntityManager();
    em.persist(artist);
    em.persist(album);
    em.flush();

    // sanity check
    assertNotNull(album.getId());

    // ensure everything's in the persistence context
    assertSame(artist, em.find(Artist.class, artist.getId()));
    assertSame(album, em.find(Album.class, album.getId()));

    em.clear();

    Album fetchedAlbum2 = em.find(Album.class, album.getId());
    assertNull(fetchedAlbum2.getArtist());
  }

  public void testFetchAssociatedEntityAlreadyInPersistenceContext() {
    // make them
    Artist artist = new Artist();
    artist.setId(99L); // Artist uses user-assigned/non-generated IDs
    artist.setName("The Beatles");

    Genre rock = new Genre("Rock");
    artist.addGenre(rock);

    // store it
    EntityManager em = getEntityManager();
    em.persist(artist); // should cascade onto Rock genre, which is in the collection relation
    em.flush();

    assertNotNull(rock.getId());

    // ensure both are in the persistence context
    assertSame(artist, em.find(Artist.class, artist.getId()));
    assertSame(rock, em.find(Genre.class, rock.getId()));

    em.clear();

    // prefetch the genre to get it into the persistence context
    Genre fetchedRock = em.find(Genre.class, rock.getId());
    assertNotNull(fetchedRock);

    Artist fetchedArtist = em.find(Artist.class, artist.getId());
    assertNotNull(fetchedArtist);

    assertEquals(1, fetchedArtist.getGenres().size());
    Genre cascadeFetchedGenre = fetchedArtist.getGenres().iterator().next();
    assertSame(fetchedRock, cascadeFetchedGenre);
  }

  public void testPersistNewEntityLifecycle() throws Exception {

    // make it
    Album album = new Album();
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    List<CallbackLogEntry> expectedLifecycle = new ArrayList<CallbackLogEntry>();
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);

    // the standalone listener is always notified before the entity itself (JPA2 section 3.5.4)
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PrePersist.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PrePersist.class));

    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PostPersist.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PostPersist.class));
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);

    em.flush();
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);

    // verify that detach causes no lifecycle updates
    em.detach(album);
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);
  }

  public void testFetchEntityLifecycle() throws Exception {

    // make it
    Album album = new Album();
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();
    em.detach(album);

    List<CallbackLogEntry> expectedLifecycle = new ArrayList<CallbackLogEntry>();
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PrePersist.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PrePersist.class));
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PostPersist.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PostPersist.class));
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);

    // fetch a fresh copy
    Album fetchedAlbum = em.find(Album.class, album.getId());
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(fetchedAlbum), PostLoad.class));
    expectedLifecycle.add(new CallbackLogEntry(fetchedAlbum, PostLoad.class));
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);

    // fetch again; expect no more PostLoad notifications
    Album fetchedAlbum2 = em.find(Album.class, album.getId());
    assertSame(fetchedAlbum, fetchedAlbum2);
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);
  }

  public void testRemoveEntityLifecycle() throws Exception {

    // make it
    Album album = new Album();
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();
    em.detach(album);

    List<CallbackLogEntry> expectedLifecycle = new ArrayList<CallbackLogEntry>();
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PrePersist.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PrePersist.class));
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PostPersist.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PostPersist.class));
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);

    // fetch a fresh copy
    Album fetchedAlbum = em.find(Album.class, album.getId());
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(fetchedAlbum), PostLoad.class));
    expectedLifecycle.add(new CallbackLogEntry(fetchedAlbum, PostLoad.class));
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);

    // delete it
    em.remove(fetchedAlbum);
    em.flush();
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(fetchedAlbum), PreRemove.class));
    expectedLifecycle.add(new CallbackLogEntry(fetchedAlbum, PreRemove.class));
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(fetchedAlbum), PostRemove.class));
    expectedLifecycle.add(new CallbackLogEntry(fetchedAlbum, PostRemove.class));
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);
  }

  public void testUpdateEntityLifecycle() throws Exception {

    // make it
    Album album = new Album();
    album.setArtist(null);
    album.setName("Abbey Road");
    album.setReleaseDate(new Date(-8366400000L));

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();

    List<CallbackLogEntry> expectedLifecycle = new ArrayList<CallbackLogEntry>();

    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PrePersist.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PrePersist.class));
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PostPersist.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PostPersist.class));
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);

    // modify it
    album.setName("Cowabunga");
    em.flush();

    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PreUpdate.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PreUpdate.class));
    expectedLifecycle.add(new CallbackLogEntry(StandaloneLifecycleListener.instanceFor(album), PostUpdate.class));
    expectedLifecycle.add(new CallbackLogEntry(album, PostUpdate.class));
    assertEquals(expectedLifecycle, Album.CALLBACK_LOG);
  }

  public void testStoreAndFetchOneWithEverything() throws Exception {
    Timestamp timestamp = new Timestamp(1234L);
    timestamp.setNanos(4321);

    Zentity original = new Zentity(
            true, Boolean.FALSE,
            (byte) -10, Byte.valueOf((byte) -10), new byte[] { -128, 0, 127, 126, 125, 124 }, new Byte[] { -128, 0, 127, -3 },
            'a', 'a', new char[] {'\u1234', '\u0000', 'a' }, new Character[] {'\u1234', '\u0000', 'a' },
            Short.MIN_VALUE, Short.valueOf(Short.MIN_VALUE),
            Integer.MIN_VALUE, Integer.valueOf(Integer.MIN_VALUE),
            Long.MIN_VALUE, Long.valueOf(Long.MIN_VALUE),
            Float.MIN_VALUE, Float.valueOf(Float.MIN_VALUE),
            Double.MIN_VALUE, Double.valueOf(Double.MIN_VALUE),
            "A string with \u4292 non-ascii char",
            BigInteger.TEN, BigDecimal.TEN,
            new java.util.Date(1234L), new java.sql.Date(1234L), new Time(1234L), timestamp,
            PersistenceContextType.TRANSACTION);

    // store it
    EntityManager em = getEntityManager();
    em.persist(original);
    em.flush();

    assertNotNull(original.getId());

    em.clear();

    Zentity fetched = em.find(Zentity.class, original.getId());
    assertNotSame(original, fetched);
    assertEquals(original.toString(), fetched.toString());
  }

  /**
   * This is to ensure that the null value of all nullable types can be marshalled and demarshalled without incident.
   */
  public void testStoreAndFetchOneWithEverythingDefaultValues() throws Exception {
    Zentity original = new Zentity();

    // store it
    EntityManager em = getEntityManager();
    em.persist(original);
    em.flush();

    assertNotNull(original.getId());

    em.clear();

    Zentity fetched = em.find(Zentity.class, original.getId());
    assertNotSame(original, fetched);
    assertEquals(original.toString(), fetched.toString());
  }

  /**
   * Ensures the ErraiEntityManager transparently recognizes wrapped/proxied
   * entities.
   */
  public void testPersistProxiedEntity() {

    // make it
    Album album = new Album();

    class AlbumProxy extends Album implements WrappedPortable {

      private final Album wrapped;

      AlbumProxy(Album wrapme) {
        wrapped = wrapme;
      }

      @Override
      public Object unwrap() {
        return wrapped;
      }

      @Override
      public Long getId() {
        return wrapped.getId();
      }

      @Override
      public void setId(Long id) {
        wrapped.setId(id);
      }

      @Override
      public String getName() {
        return wrapped.getName();
      }

      @Override
      public Artist getArtist() {
        return wrapped.getArtist();
      }

      @Override
      public Date getReleaseDate() {
        return wrapped.getReleaseDate();
      }

      @Override
      public void setName(String name) {
        wrapped.setName(name);
      }

      @Override
      public void setArtist(Artist artist) {
        wrapped.setArtist(artist);
      }

      @Override
      public void setReleaseDate(Date releaseDate) {
        wrapped.setReleaseDate(releaseDate);
      }

      @Override
      public Format getFormat() {
        return wrapped.getFormat();
      }

      @Override
      public void setFormat(Format format) {
        wrapped.setFormat(format);
      }

      @Override
      public int hashCode() {
        return wrapped.hashCode();
      }

      @Override
      public String toString() {
        return wrapped.toString();
      }

      @Override
      public void postLoad() {
        wrapped.postLoad();
      }

      @Override
      public boolean equals(Object obj) {
        return wrapped.equals(obj);
      }
    }

    album = new AlbumProxy(album);

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();
    em.detach(album);
    assertNotNull(album.getId());

    // fetch it
    Album fetchedAlbum = em.find(Album.class, album.getId());
    assertNotSame(album, fetchedAlbum);
    assertEquals(album.toString(), fetchedAlbum.toString());
  }

  /**
   * Ensures the ErraiEntityManager transparently recognizes wrapped/proxied
   * entities.
   */
  public void testUpdateDataBinderProxiedEntity() {

    // make it
    Album album = new Album();
    TextBox box = new TextBox();

    DataBinder<Album> binder = DataBinder.forModel(album);
    binder.bind(box, "id");
    album = binder.getModel();
    assertEquals("", box.getText());

    // store it
    EntityManager em = getEntityManager();
    em.persist(album);
    em.flush();
    em.detach(album);
    assertNotNull(album.getId());
    assertEquals(String.valueOf(album.getId()), box.getText());
  }

  public void testNullCollectionInEntity() throws Exception {
    Artist artist = new Artist();
    artist.setId(4433443L);
    artist.setGenres(null);
    // store it
    EntityManager em = getEntityManager();
    em.persist(artist);
    em.flush();
    em.detach(artist);

    Artist fetchedArtist = em.find(Artist.class, artist.getId());
    assertNotNull(fetchedArtist.getGenres());
    assertEquals(0, fetchedArtist.getGenres().size());
  }

  public void testNullSingularReferenceInEntity() throws Exception {
    CascadeFrom from = new CascadeFrom();
    from.setAll(null);

    // store it
    EntityManager em = getEntityManager();
    em.persist(from);
    em.flush();
    em.detach(from);

    CascadeFrom fetchedFrom = em.find(CascadeFrom.class, Long.valueOf(from.getId()));
    assertNull(fetchedFrom.getAll());
  }

}
