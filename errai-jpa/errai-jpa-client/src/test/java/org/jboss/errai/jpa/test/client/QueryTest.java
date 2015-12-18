/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.test.client;


import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.rebind.ErraiEntityManagerGenerator;
import org.jboss.errai.jpa.test.client.res.JpaClientTestCase;
import org.jboss.errai.jpa.test.entity.Album;
import org.jboss.errai.jpa.test.entity.Artist;
import org.jboss.errai.jpa.test.entity.Format;
import org.jboss.errai.jpa.test.entity.Zentity;

/**
 * Tests the query behaviour of Errai JPA.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class QueryTest extends JpaClientTestCase {

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

    new IOCBeanManagerLifecycle().resetBeanManager();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().bootstrapContainer();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    Container.reset();
    IOC.reset();
  }

  /**
   * Tests that the entity manager was injected into the testing class. If this
   * test fails, the likely cause is that the
   * {@link ErraiEntityManagerGenerator} failed to output a compilable class. In
   * that case, try re-running this test with
   * {@code -Derrai.codegen.permissive=true} and
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

  /**
   * This test is being ignored for now because Hibernate does not support the
   * JPA2 literal date syntax <tt>{d 'yyyy-mm-dd'}</tt>. It would be possible,
   * but trickier than we want, to support date literals in the Hibernate format
   * of {@code 'yyyy-mm-dd'}. The workaround is to write queries with named
   * parameters in place of literal dates.
   */
  @SuppressWarnings("deprecation")
  public void IGNOREtestFilterByLiteralDate() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setDate(new Date(112, 5, 22));
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setDate(new Date(115, 0, 1));
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralDate", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByLiteralLong() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveLong(11223344L);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveLong(12345);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralLong", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByLiteralInteger() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveInt(-55443322);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveInt(12345);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralInt", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByLiteralShort() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveShort((short) -1234);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveShort((short) 12345);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralShort", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByLiteralChar() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveChar('c');
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveChar('a');
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralChar", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByLiteralByte() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveByte((byte) -5);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveByte((byte) 123);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralByte", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByLiteralDouble() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveDouble(123.45);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveDouble(123.0);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralDouble", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByLiteralFloat() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveFloat(-1234.5f);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveFloat(123.0f);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralFloat", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testFilterDoubleByLiteralInt() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveDouble(12345.0);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveDouble(123.0);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralDoubleToInt", Zentity.class);

    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByLiteralBool() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveBool(true);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveBool(false);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralBoolTrue", Zentity.class);
    assertEquals(zentity1.toString(), q.getSingleResult().toString());

    TypedQuery<Zentity> q2 = em.createNamedQuery("zentityLiteralBoolFalse", Zentity.class);
    assertEquals(zentity2.toString(), q2.getSingleResult().toString());
  }

  public void testFilterByLiteralNull() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString(null);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("this is not null");
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLiteralNull", Zentity.class);
    assertEquals(zentity1.toString(), q.getSingleResult().toString());

    TypedQuery<Zentity> q2 = em.createNamedQuery("zentityLiteralNotNull", Zentity.class);
    assertEquals(zentity2.toString(), q2.getSingleResult().toString());
  }

  public void testFilterByLiteralEnum() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Album album1 = new Album();
    album1.setName("Hey Jude / Revolution");
    album1.setFormat(Format.SINGLE);
    album1.setReleaseDate(new Date(-42580800000L));
    em.persist(album1);

    Album album2 = new Album();
    album2.setName("Let It Be");
    album2.setFormat(Format.LP);
    album2.setReleaseDate(new Date(11012400000L));

    em.flush();

    TypedQuery<Album> q = em.createNamedQuery("albumLiteralEnum", Album.class);
    assertEquals(album1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByEntityReference() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Artist beatles = new Artist();
    beatles.setId(9L);
    beatles.setName("The Beatles");

    Album album1 = new Album();
    album1.setArtist(beatles);
    album1.setName("Hey Jude / Revolution");
    album1.setFormat(Format.SINGLE);
    album1.setReleaseDate(new Date(-42580800000L));

    Album album2 = new Album();
    album2.setArtist(null);
    album2.setName("Marcel Marceau's Greatest Hits");
    album2.setFormat(Format.LP);
    album2.setReleaseDate(new Date(-32580800000L));

    em.persist(beatles);
    em.persist(album1);
    em.persist(album2);

    em.flush();

    TypedQuery<Album> q = em.createNamedQuery("selectAlbumByArtist", Album.class);

    // note that Artist doesn't implement equals(), so this test only works as long as album1 is still in the persistence context.
    q.setParameter("artist", beatles);

    assertEquals(album1.toString(), q.getSingleResult().toString());
  }

  public void testFilterByNullEntityReference() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Artist beatles = new Artist();
    beatles.setId(9L);
    beatles.setName("The Beatles");

    Album album1 = new Album();
    album1.setArtist(beatles);
    album1.setName("Hey Jude / Revolution");
    album1.setFormat(Format.SINGLE);
    album1.setReleaseDate(new Date(-42580800000L));

    Album album2 = new Album();
    album2.setArtist(null);
    album2.setName("Marcel Marceau's Greatest Hits");
    album2.setFormat(Format.LP);
    album2.setReleaseDate(new Date(-32580800000L));

    em.persist(beatles);
    em.persist(album1);
    em.persist(album2);

    em.flush();

    TypedQuery<Album> q = em.createNamedQuery("selectAlbumByArtist", Album.class);
    q.setParameter("artist", null);

    assertTrue(q.getResultList().isEmpty());
  }

  public void testAnd() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    zentity1.setPrimitiveInt(555);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("goodbye");
    zentity2.setPrimitiveInt(555);
    zentity2.setPrimitiveByte((byte) 2);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityAnd", Zentity.class);
    assertEquals(zentity1.toString(), q.getSingleResult().toString());

  }

  public void testOr() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    zentity1.setPrimitiveInt(555);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("goodbye");
    zentity2.setPrimitiveInt(555);
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("goodbye");
    zentity3.setPrimitiveInt(556);
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityOr", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(2, resultStrings.size());
    assertTrue(resultStrings.contains(zentity1.toString()));
    assertTrue(resultStrings.contains(zentity2.toString()));
    assertFalse(resultStrings.contains(zentity3.toString()));
  }

  public void testNot() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("goodbye");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("hello");
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityNot", Zentity.class);
    assertEquals(zentity1.toString(), q.getSingleResult().toString());

  }

  public void testNestedBooleanLogic() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    zentity1.setPrimitiveInt(555);
    zentity1.setPrimitiveByte((byte) 1);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("goodbye");
    zentity2.setPrimitiveInt(555);
    zentity2.setPrimitiveByte((byte) 2);
    em.persist(zentity2);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityNestedBooleanLogic", Zentity.class);
    assertEquals(zentity1.toString(), q.getSingleResult().toString());
  }

  public void testNumericGreaterThan() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveInt(555);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveInt(556);
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setPrimitiveInt(554);
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityGreaterThan", Zentity.class);
    assertEquals(zentity2.toString(), q.getSingleResult().toString());
  }

  public void testNumericGreaterThanOrEqualTo() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveInt(555);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveInt(556);
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setPrimitiveInt(554);
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityGreaterThanOrEqualTo", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(2, resultStrings.size());
    assertTrue(resultStrings.contains(zentity1.toString()));
    assertTrue(resultStrings.contains(zentity2.toString()));
    assertFalse(resultStrings.contains(zentity3.toString()));
  }

  public void testNumericLessThan() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveInt(555);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveInt(556);
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setPrimitiveInt(554);
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLessThan", Zentity.class);
    assertEquals(zentity3.toString(), q.getSingleResult().toString());
  }

  public void testNumericLessThanOrEqualTo() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveInt(555);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveInt(556);
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setPrimitiveInt(554);
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLessThanOrEqualTo", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(2, resultStrings.size());
    assertTrue(resultStrings.contains(zentity1.toString()));
    assertFalse(resultStrings.contains(zentity2.toString()));
    assertTrue(resultStrings.contains(zentity3.toString()));
  }

  public void testStringGreaterThan() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("impostor");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("goodbye");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityStringGreaterThan", Zentity.class);
    assertEquals(zentity2.toString(), q.getSingleResult().toString());
  }

  public void testStringGreaterThanOrEqualTo() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("impostor");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("goodbye");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityStringGreaterThanOrEqualTo", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(2, resultStrings.size());
    assertTrue(resultStrings.contains(zentity1.toString()));
    assertTrue(resultStrings.contains(zentity2.toString()));
    assertFalse(resultStrings.contains(zentity3.toString()));
  }

  public void testStringLessThan() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("impostor");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("goodbye");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityStringLessThan", Zentity.class);
    assertEquals(zentity3.toString(), q.getSingleResult().toString());
  }

  public void testStringLessThanOrEqualTo() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("impostor");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("goodbye");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityStringLessThanOrEqualTo", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(2, resultStrings.size());
    assertTrue(resultStrings.contains(zentity1.toString()));
    assertFalse(resultStrings.contains(zentity2.toString()));
    assertTrue(resultStrings.contains(zentity3.toString()));
  }

  public void testInLiteral() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("baz");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityInLiteral", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(2, resultStrings.size());
    assertFalse(resultStrings.contains(zentity1.toString()));
    assertTrue(resultStrings.contains(zentity2.toString()));
    assertTrue(resultStrings.contains(zentity3.toString()));
  }

  public void testNotInLiteral() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("baz");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityNotInLiteral", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(1, resultStrings.size());
    assertTrue(resultStrings.contains(zentity1.toString()));
    assertFalse(resultStrings.contains(zentity2.toString()));
    assertFalse(resultStrings.contains(zentity3.toString()));
  }

  public void testInSingleValuedParams() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("baz");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityInSingleValuedParams", Zentity.class);
    q.setParameter("in1", "foo");
    q.setParameter("in2", "bar");
    q.setParameter("in3", "baz");
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(2, resultStrings.size());
    assertFalse(resultStrings.contains(zentity1.toString()));
    assertTrue(resultStrings.contains(zentity2.toString()));
    assertTrue(resultStrings.contains(zentity3.toString()));
  }

  public void testInCollectionParam() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("hello");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("baz");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityInCollectionParam", Zentity.class);
    q.setParameter("inList", Arrays.asList(new String[] {"hello", "foo", "this one doesn't match"}));
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(2, resultStrings.size());
    assertTrue(resultStrings.contains(zentity1.toString()));
    assertTrue(resultStrings.contains(zentity2.toString()));
    assertFalse(resultStrings.contains(zentity3.toString()));
  }

  public void testNumericBetween() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setBoxedDouble(1.0);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setBoxedDouble(2.0);
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setBoxedDouble(3.0);
    em.persist(zentity3);

    Zentity zentity4 = new Zentity();
    zentity4.setBoxedDouble(4.0);
    em.persist(zentity4);

    Zentity zentity5 = new Zentity();
    zentity5.setBoxedDouble(5.0);
    em.persist(zentity5);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityBetween", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(3, resultStrings.size());
    assertFalse(resultStrings.contains(zentity1.toString()));
    assertTrue(resultStrings.contains(zentity2.toString()));
    assertTrue(resultStrings.contains(zentity3.toString()));
    assertTrue(resultStrings.contains(zentity4.toString()));
    assertFalse(resultStrings.contains(zentity5.toString()));
  }

  public void testNumericNotBetween() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setBoxedDouble(1.0);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setBoxedDouble(2.0);
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setBoxedDouble(3.0);
    em.persist(zentity3);

    Zentity zentity4 = new Zentity();
    zentity4.setBoxedDouble(4.0);
    em.persist(zentity4);

    Zentity zentity5 = new Zentity();
    zentity5.setBoxedDouble(5.0);
    em.persist(zentity5);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityNotBetween", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(2, resultStrings.size());
    assertTrue(resultStrings.contains(zentity1.toString()));
    assertFalse(resultStrings.contains(zentity2.toString()));
    assertFalse(resultStrings.contains(zentity3.toString()));
    assertFalse(resultStrings.contains(zentity4.toString()));
    assertTrue(resultStrings.contains(zentity5.toString()));
  }

  public void testNoWhereClauseSelectsEverything() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Album album = new Album();
    album.setName("Don't select me!");
    em.persist(album);

    Zentity zentity1 = new Zentity();
    zentity1.setBoxedDouble(1.0);
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setBoxedDouble(2.0);
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setBoxedDouble(3.0);
    em.persist(zentity3);

    Zentity zentity4 = new Zentity();
    zentity4.setBoxedDouble(4.0);
    em.persist(zentity4);

    Zentity zentity5 = new Zentity();
    zentity5.setBoxedDouble(5.0);
    em.persist(zentity5);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityNoWhereClause", Zentity.class);
    Set<String> resultStrings = new HashSet<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(5, resultStrings.size());
    assertTrue(resultStrings.contains(zentity1.toString()));
    assertTrue(resultStrings.contains(zentity2.toString()));
    assertTrue(resultStrings.contains(zentity3.toString()));
    assertTrue(resultStrings.contains(zentity4.toString()));
    assertTrue(resultStrings.contains(zentity5.toString()));
  }

  public void testOrderByPrimitiveInt() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity5 = new Zentity();
    zentity5.setPrimitiveInt(5);
    em.persist(zentity5);

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveInt(1);
    em.persist(zentity1);

    Zentity zentity3 = new Zentity();
    zentity3.setPrimitiveInt(3);
    em.persist(zentity3);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveInt(2);
    em.persist(zentity2);

    Zentity zentity4 = new Zentity();
    zentity4.setPrimitiveInt(4);
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityOrderByPrimitiveInt", Zentity.class);
    List<String> resultStrings = new ArrayList<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(5, resultStrings.size());
    assertEquals(resultStrings.get(0), zentity1.toString());
    assertEquals(resultStrings.get(1), zentity2.toString());
    assertEquals(resultStrings.get(2), zentity3.toString());
    assertEquals(resultStrings.get(3), zentity4.toString());
    assertEquals(resultStrings.get(4), zentity5.toString());
  }

  public void testOrderByPrimitiveIntDesc() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity5 = new Zentity();
    zentity5.setPrimitiveInt(5);
    em.persist(zentity5);

    Zentity zentity1 = new Zentity();
    zentity1.setPrimitiveInt(1);
    em.persist(zentity1);

    Zentity zentity3 = new Zentity();
    zentity3.setPrimitiveInt(3);
    em.persist(zentity3);

    Zentity zentity2 = new Zentity();
    zentity2.setPrimitiveInt(2);
    em.persist(zentity2);

    Zentity zentity4 = new Zentity();
    zentity4.setPrimitiveInt(4);
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityOrderByPrimitiveIntDesc", Zentity.class);
    List<String> resultStrings = new ArrayList<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(5, resultStrings.size());
    assertEquals(resultStrings.get(0), zentity5.toString());
    assertEquals(resultStrings.get(1), zentity4.toString());
    assertEquals(resultStrings.get(2), zentity3.toString());
    assertEquals(resultStrings.get(3), zentity2.toString());
    assertEquals(resultStrings.get(4), zentity1.toString());
  }

  public void testOrderByStringDescThenInt() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity5 = new Zentity();
    zentity5.setString("b");
    zentity5.setPrimitiveInt(5);
    em.persist(zentity5);

    Zentity zentity1 = new Zentity();
    zentity1.setString("a");
    zentity1.setPrimitiveInt(1);
    em.persist(zentity1);

    Zentity zentity3 = new Zentity();
    zentity3.setString("a");
    zentity3.setPrimitiveInt(3);
    em.persist(zentity3);

    Zentity zentity2 = new Zentity();
    zentity2.setString("b");
    zentity2.setPrimitiveInt(2);
    em.persist(zentity2);

    Zentity zentity4 = new Zentity();
    zentity4.setString("a");
    zentity4.setPrimitiveInt(4);
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityOrderByStringDescThenInt", Zentity.class);
    List<String> resultStrings = new ArrayList<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(5, resultStrings.size());
    assertEquals(resultStrings.get(0), zentity2.toString());
    assertEquals(resultStrings.get(1), zentity5.toString());
    assertEquals(resultStrings.get(2), zentity1.toString());
    assertEquals(resultStrings.get(3), zentity3.toString());
    assertEquals(resultStrings.get(4), zentity4.toString());
  }

  public void testOrderByStringAscThenInt() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity5 = new Zentity();
    zentity5.setString("b");
    zentity5.setPrimitiveInt(5);
    em.persist(zentity5);

    Zentity zentity1 = new Zentity();
    zentity1.setString("a");
    zentity1.setPrimitiveInt(1);
    em.persist(zentity1);

    Zentity zentity3 = new Zentity();
    zentity3.setString("a");
    zentity3.setPrimitiveInt(3);
    em.persist(zentity3);

    Zentity zentity2 = new Zentity();
    zentity2.setString("b");
    zentity2.setPrimitiveInt(2);
    em.persist(zentity2);

    Zentity zentity4 = new Zentity();
    zentity4.setString("a");
    zentity4.setPrimitiveInt(4);
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityOrderByStringAscThenInt", Zentity.class);
    List<String> resultStrings = new ArrayList<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(5, resultStrings.size());
    assertEquals(resultStrings.get(0), zentity1.toString());
    assertEquals(resultStrings.get(1), zentity3.toString());
    assertEquals(resultStrings.get(2), zentity4.toString());
    assertEquals(resultStrings.get(3), zentity2.toString());
    assertEquals(resultStrings.get(4), zentity5.toString());
  }

  public void testOrderByStringThenInt() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity5 = new Zentity();
    zentity5.setString("b");
    zentity5.setPrimitiveInt(5);
    em.persist(zentity5);

    Zentity zentity1 = new Zentity();
    zentity1.setString("a");
    zentity1.setPrimitiveInt(1);
    em.persist(zentity1);

    Zentity zentity3 = new Zentity();
    zentity3.setString("a");
    zentity3.setPrimitiveInt(3);
    em.persist(zentity3);

    Zentity zentity2 = new Zentity();
    zentity2.setString("b");
    zentity2.setPrimitiveInt(2);
    em.persist(zentity2);

    Zentity zentity4 = new Zentity();
    zentity4.setString("a");
    zentity4.setPrimitiveInt(4);
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityOrderByStringThenInt", Zentity.class);
    List<String> resultStrings = new ArrayList<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(5, resultStrings.size());
    assertEquals(resultStrings.get(0), zentity1.toString());
    assertEquals(resultStrings.get(1), zentity3.toString());
    assertEquals(resultStrings.get(2), zentity4.toString());
    assertEquals(resultStrings.get(3), zentity2.toString());
    assertEquals(resultStrings.get(4), zentity5.toString());
  }

  public void testOrderByWithNulls() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity5 = new Zentity();
    zentity5.setBoxedFloat(5f);
    em.persist(zentity5);

    Zentity zentity1 = new Zentity();
    zentity1.setBoxedFloat(1f);
    em.persist(zentity1);

    Zentity zentity3 = new Zentity();
    zentity3.setBoxedFloat(null);
    em.persist(zentity3);

    Zentity zentity2 = new Zentity();
    zentity2.setBoxedFloat(2f);
    em.persist(zentity2);

    Zentity zentity4 = new Zentity();
    zentity4.setBoxedFloat(4f);
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityOrderByBoxedFloat", Zentity.class);
    List<String> resultStrings = new ArrayList<String>();
    for (Zentity z : q.getResultList()) {
      resultStrings.add(z.toString());
    }
    assertEquals(5, resultStrings.size());
    assertEquals(resultStrings.get(0), zentity3.toString());
    assertEquals(resultStrings.get(1), zentity1.toString());
    assertEquals(resultStrings.get(2), zentity2.toString());
    assertEquals(resultStrings.get(3), zentity4.toString());
    assertEquals(resultStrings.get(4), zentity5.toString());
  }

  public void testLowercaseFunction() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("Foo");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("bar");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLowercaseFunction", Zentity.class);
    List<Zentity> results = q.getResultList();
    assertEquals(2, results.size());
    assertTrue(results.contains(zentity1));
    assertTrue(results.contains(zentity2));
  }

  public void testUppercaseFunction() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("Foo");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("bar");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityUppercaseFunction", Zentity.class);
    List<Zentity> results = q.getResultList();
    assertEquals(2, results.size());
    assertTrue(results.contains(zentity1));
    assertTrue(results.contains(zentity2));
  }

  public void testParamNestedInFunction() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("Foo");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("bar");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityParamNestedInFunction", Zentity.class);
    q.setParameter("str", "fOO");
    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertTrue(results.contains(zentity2));
  }

  public void testConcatFunction() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("F");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("f");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("b");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityConcatFunction", Zentity.class);
    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertFalse(results.contains(zentity1));
    assertTrue(results.contains(zentity2));
    assertFalse(results.contains(zentity3));
  }

  public void testSubstringFunctionOneArg() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("ala b");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("raffe");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("donkey");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentitySubstringFunctionOneArg", Zentity.class);
    q.setParameter("bigStr", "giraffe");
    q.setParameter("startPos", 3);

    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertFalse(results.contains(zentity1));
    assertTrue(results.contains(zentity2));
    assertFalse(results.contains(zentity3));
  }

  public void testSubstringFunctionTwoArgs() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("ala b");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("raffe");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("donkey");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentitySubstringFunctionTwoArgs", Zentity.class);
    q.setParameter("bigStr", "koala bear");
    q.setParameter("startPos", 3);
    q.setParameter("length", 5);

    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertTrue(results.contains(zentity1));
    assertFalse(results.contains(zentity2));
    assertFalse(results.contains(zentity3));
  }

  public void testTrimFunction() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("   foo   ");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("  foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("foo ");
    em.persist(zentity3);

    Zentity zentity4 = new Zentity();
    zentity4.setString(" bar ");
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityTrimFunction", Zentity.class);

    List<Zentity> results = q.getResultList();
    assertEquals(3, results.size());
    assertTrue(results.contains(zentity1));
    assertTrue(results.contains(zentity2));
    assertTrue(results.contains(zentity3));
    assertFalse(results.contains(zentity4));
  }

  public void testTrimLeadingFunction() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("   foo   ");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("  foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("foo ");
    em.persist(zentity3);

    Zentity zentity4 = new Zentity();
    zentity4.setString(" bar ");
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityTrimLeadingFunction", Zentity.class);

    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertFalse(results.contains(zentity1));
    assertTrue(results.contains(zentity2));
    assertFalse(results.contains(zentity3));
    assertFalse(results.contains(zentity4));
  }

  public void testTrimTrailingFunction() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("   foo   ");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("  foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("foo ");
    em.persist(zentity3);

    Zentity zentity4 = new Zentity();
    zentity4.setString(" bar ");
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityTrimTrailingFunction", Zentity.class);

    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertFalse(results.contains(zentity1));
    assertFalse(results.contains(zentity2));
    assertTrue(results.contains(zentity3));
    assertFalse(results.contains(zentity4));
  }

  public void testTrimTrailingWithCustomPadFunction() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("   foo   ");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("  foo");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("foo");
    em.persist(zentity3);

    Zentity zentity4 = new Zentity();
    zentity4.setString(" bar ");
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityTrimTrailingWithCustomPadFunction", Zentity.class);

    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertFalse(results.contains(zentity1));
    assertFalse(results.contains(zentity2));
    assertTrue(results.contains(zentity3));
    assertFalse(results.contains(zentity4));
  }

  public void testLengthFunction() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("Foo");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("bar");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("foobar");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLengthFunction", Zentity.class);
    List<Zentity> results = q.getResultList();
    assertEquals(2, results.size());
    assertTrue(results.contains(zentity1));
    assertTrue(results.contains(zentity2));
    assertFalse(results.contains(zentity3));
  }

  public void testLocateFunction2Args() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("Foo");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("bar");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("foobar");
    em.persist(zentity3);

    Zentity zentity4 = new Zentity();
    zentity4.setString("baboon");
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLocateFunction2Args", Zentity.class);
    q.setParameter("lookFor", "oo");
    List<Zentity> results = q.getResultList();
    assertEquals(2, results.size());
    assertTrue(results.contains(zentity1));
    assertFalse(results.contains(zentity2));
    assertTrue(results.contains(zentity3));
    assertFalse(results.contains(zentity4));
  }

  public void testLocateFunction3Args() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("Foo");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("bar");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString("foobar");
    em.persist(zentity3);

    Zentity zentity4 = new Zentity();
    zentity4.setString("baboon");
    em.persist(zentity4);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLocateFunction3Args", Zentity.class);
    q.setParameter("lookFor", "oo");
    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertFalse(results.contains(zentity1));
    assertFalse(results.contains(zentity2));
    assertFalse(results.contains(zentity3));
    assertTrue(results.contains(zentity4));
  }

  public void testLikeOperator() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentityFoo = new Zentity();
    zentityFoo.setString("Foo");
    em.persist(zentityFoo);

    Zentity zentityfoo = new Zentity();
    zentityfoo.setString("foo");
    em.persist(zentityfoo);

    Zentity zentitybar = new Zentity();
    zentitybar.setString("bar");
    em.persist(zentitybar);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLike", Zentity.class);
    q.setParameter("str", "f%o");
    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertTrue(results.contains(zentityfoo));
  }

  public void testLikeOperatorRegexCharsOk() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("!@#$%^&*()_+");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("+_)(*&^%$#@!");
    em.persist(zentity2);

    Zentity zentity3 = new Zentity();
    zentity3.setString(",./<>?[]{};':\"\\");
    em.persist(zentity3);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLike", Zentity.class);
    q.setParameter("str", "%(__+");
    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertTrue(results.contains(zentity1));

    q.setParameter("str", ",./<>?[]{};':\"\\");
    results = q.getResultList();
    assertEquals(1, results.size());
    assertTrue(results.contains(zentity3));
  }

  public void testNotLikeOperator() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentityFoo = new Zentity();
    zentityFoo.setString("Foo");
    em.persist(zentityFoo);

    Zentity zentityfoo = new Zentity();
    zentityfoo.setString("foo");
    em.persist(zentityfoo);

    Zentity zentitybar = new Zentity();
    zentitybar.setString("bar");
    em.persist(zentitybar);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityNotLike", Zentity.class);
    q.setParameter("str", "f%o");
    List<Zentity> results = q.getResultList();
    assertEquals(2, results.size());
    assertTrue(results.contains(zentityFoo));
    assertTrue(results.contains(zentitybar));
  }

  public void testLikeOperatorWithEscape() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    Zentity zentity1 = new Zentity();
    zentity1.setString("wx%yz");
    em.persist(zentity1);

    Zentity zentity2 = new Zentity();
    zentity2.setString("wx!yz");
    em.persist(zentity2);

    Zentity zentitybar = new Zentity();
    zentitybar.setString("bar");
    em.persist(zentitybar);

    em.flush();

    TypedQuery<Zentity> q = em.createNamedQuery("zentityLikeWithEscapeChar", Zentity.class);

    // note that the query sets the escape character for the LIKE expression to 'a'
    q.setParameter("str", "wxa%yz");

    List<Zentity> results = q.getResultList();
    assertEquals(1, results.size());
    assertTrue(results.contains(zentity1));
  }

}
