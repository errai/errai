package org.jboss.errai.jpa.test.client;


import java.sql.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.rebind.ErraiEntityManagerGenerator;
import org.jboss.errai.jpa.test.entity.Album;
import org.jboss.errai.jpa.test.entity.Artist;
import org.jboss.errai.jpa.test.entity.Zentity;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests the query behaviour of Errai JPA.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class QueryTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  protected EntityManager getEntityManagerAndClearStorageBackend() {
    JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    assertNotNull(testClient.entityManager);
    ((ErraiEntityManager) testClient.entityManager).removeAll();
    return testClient.entityManager;
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

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
    getEntityManagerAndClearStorageBackend(); // has its own assertions
  }

  public void testNonExistentQuery() throws Exception {
    try {
      getEntityManagerAndClearStorageBackend().createNamedQuery("DoesNotExist", Object.class);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("DoesNotExist"));
    }
  }

  public void testQueryWithWrongResultType() throws Exception {
    try {
      getEntityManagerAndClearStorageBackend().createNamedQuery("selectAlbumByName", Artist.class);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Artist"));
      assertTrue(e.getMessage().contains("Album"));
    }
  }

  public void testSelectAlbumByName() throws Exception {

    // make Album
    Album album = new Album();
    album.setArtist(null);
    album.setName("Let It Be");
    album.setReleaseDate(new Date(11012400000L));

    // store it and chuck it
    EntityManager em = getEntityManagerAndClearStorageBackend();
    em.persist(album);
    em.flush();
    em.clear();

    // now look it up
    TypedQuery<Album> q = em.createNamedQuery("selectAlbumByName", Album.class);
    q.setParameter("name", "Let It Be");
    List<Album> fetchedAlbums = q.getResultList();

    assertTrue("Expected >0 albums, got " + fetchedAlbums.size(), fetchedAlbums.size() > 0);

    for (Album a : fetchedAlbums) {
      assertNotNull(a);
      assertEquals("Let It Be", a.getName());
      assertNotSame(album, a);
    }
  }

  /**
   * Ensures that query results come from the entity manager's persistence
   * context (that is, redundant instances are not created).
   */
  public void testQueryDrawsFromPersistenceContext() {
    // make Album
    Album album = new Album();
    album.setArtist(null);
    album.setName("The Beatles");
    album.setReleaseDate(new Date(11012400000L));

    // store it and chuck it
    EntityManager em = getEntityManagerAndClearStorageBackend();
    em.persist(album);

    em.flush();

    TypedQuery<Album> q = em.createNamedQuery("selectAlbumByName", Album.class);
    q.setParameter("name", "The Beatles");
    List<Album> fetchedAlbums = q.getResultList();

    // ensure the one result we got was the album that's still in the persistence context
    assertEquals(1, fetchedAlbums.size());
    assertSame(album, fetchedAlbums.get(0));
  }

  public void testFilterByPrimitiveBoolean() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentityTrue = new Zentity();
    zentityTrue.setPrimitiveBool(true);
    em.persist(zentityTrue);

    Zentity zentityFalse = new Zentity();
    zentityFalse.setPrimitiveBool(false);
    em.persist(zentityFalse);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityPrimitiveBoolean", Zentity.class);

    q.setParameter("b", false);
    Zentity fetchedZentityFalse = q.getSingleResult();
    assertEquals(zentityFalse.toString(), fetchedZentityFalse.toString());

    q.setParameter("b", true);
    Zentity fetchedZentityTrue = q.getSingleResult();
    assertEquals(zentityTrue.toString(), fetchedZentityTrue.toString());
  }

  public void testFilterByString() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentityAbc = new Zentity();
    zentityAbc.setString("abc");
    em.persist(zentityAbc);

    Zentity zentityDef = new Zentity();
    zentityDef.setString("def");
    em.persist(zentityDef);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityString", Zentity.class);

    q.setParameter("s", "abc");
    assertEquals(zentityAbc.toString(), q.getSingleResult().toString());

    q.setParameter("s", "def");
    assertEquals(zentityDef.toString(), q.getSingleResult().toString());
  }

  public void testFilterByLiteralString() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("D'oh!");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("def");
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralString", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

}
