package org.jboss.errai.jpa.sync.test.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.jboss.errai.jpa.sync.client.shared.ConflictResponse;
import org.jboss.errai.jpa.sync.client.shared.DeleteResponse;
import org.jboss.errai.jpa.sync.client.shared.IdChangeResponse;
import org.jboss.errai.jpa.sync.client.shared.NewRemoteEntityResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation.Type;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.jpa.sync.client.shared.UpdateResponse;
import org.jboss.errai.jpa.sync.test.entity.SimpleEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SyncableDataSetUnitTest {

  private EntityManager em;

  @Before
  public void setup() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("hibernate.connection.driver_class", "org.h2.Driver");
    properties.put("hibernate.connection.url", "jdbc:h2:mem:temporary");
    properties.put("hibernate.connection.username", "sa");
    properties.put("hibernate.connection.password", "");
    properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    properties.put("hibernate.hbm2ddl.auto", "update");
    properties.put("javax.persistence.validation.mode", "none");
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("ErraiDataSyncTests", properties);
    em = emf.createEntityManager();
    em.getTransaction().begin();
  }

  @After
  public void tearDown() {
    if (em.getTransaction().isActive()) {
      em.getTransaction().rollback();
    }
  }

  @Test
  public void testSendNewSimpleEntityNoConflict() {
    TypedQuery<SimpleEntity> query = em.createQuery("SELECT se FROM SimpleEntity se", SimpleEntity.class);
    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from(em, query);

    SimpleEntity localSimpleEntity = new SimpleEntity();
    SimpleEntity.setId(localSimpleEntity, 1234L); // simulating an ID we generated in the browser; Hibernate doesn't know about it
    localSimpleEntity.setDate(new Timestamp(System.currentTimeMillis()));
    localSimpleEntity.setInteger(42);
    localSimpleEntity.setString("This was recorded on sticky tape and rust.");

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(new SyncRequestOperation<SimpleEntity>(SyncRequestOperation.Type.NEW, localSimpleEntity.clone(), null));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = sds.coldSync(syncRequest);

    // ensure the response is as expected
    assertEquals(1, syncResponse.size());
    IdChangeResponse<SimpleEntity> idChangeResponse = (IdChangeResponse<SimpleEntity>) syncResponse.get(0);
    assertEquals(1234L, idChangeResponse.getOldId());
    Long newId = idChangeResponse.getEntity().getId(); // we will verify this in the next stanza

    // ensure the entity actually got persisted
    List<SimpleEntity> queryResult = em.createQuery("SELECT se FROM SimpleEntity se", SimpleEntity.class).getResultList();
    assertEquals(1, queryResult.size());
    SimpleEntity.setId(localSimpleEntity, newId); // set local instance's ID to the new remote one from the response
    assertEquals(localSimpleEntity.toString(), queryResult.get(0).toString());
  }

  @Test
  public void testReceiveNewRemoteSimpleEntity() {
    SimpleEntity remoteSimpleEntity = new SimpleEntity();
    remoteSimpleEntity.setDate(new Timestamp(-2960391600000L));
    remoteSimpleEntity.setInteger(42);
    remoteSimpleEntity.setString("Mr. Watson--come here--I want to see you.");
    em.persist(remoteSimpleEntity);
    em.flush();
    em.detach(remoteSimpleEntity);

    TypedQuery<SimpleEntity> query = em.createQuery("SELECT se FROM SimpleEntity se", SimpleEntity.class);
    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from(em, query);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();

    // now do the actual sync (we're starting from empty on the local (requesting) side)
    List<SyncResponse<SimpleEntity>> syncResponse = sds.coldSync(syncRequest);

    // ensure the response is as expected
    assertEquals(1, syncResponse.size());
    NewRemoteEntityResponse<SimpleEntity> newRemoteEntityResponse = (NewRemoteEntityResponse<SimpleEntity>) syncResponse.get(0);
    assertEquals(remoteSimpleEntity.toString(), newRemoteEntityResponse.getEntity().toString());
  }

  @Test
  public void testUpdateBothSidesUnchanged() {
    SimpleEntity remoteSimpleEntity = new SimpleEntity();
    remoteSimpleEntity.setDate(new Timestamp(-2960391600000L));
    remoteSimpleEntity.setInteger(42);
    remoteSimpleEntity.setString("Mr. Watson--come here--I want to see you.");
    em.persist(remoteSimpleEntity);
    em.flush();
    em.detach(remoteSimpleEntity);

    SimpleEntity localSimpleEntity = remoteSimpleEntity.clone();

    TypedQuery<SimpleEntity> query = em.createQuery("SELECT se FROM SimpleEntity se", SimpleEntity.class);
    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from(em, query);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.unchanged(localSimpleEntity));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = sds.coldSync(syncRequest);

    // ensure the response is as expected (nothing to do)
    assertEquals("Got unexpected response: " + syncResponse, 0, syncResponse.size());
  }

  @Test
  public void testUpdateBothSidesChanged() {
    SimpleEntity remoteSimpleEntity = new SimpleEntity();
    remoteSimpleEntity.setDate(new Timestamp(8917200000L));
    remoteSimpleEntity.setInteger(123456);
    remoteSimpleEntity.setString("Houston, we've had a problem.");
    em.persist(remoteSimpleEntity);
    em.flush();
    em.detach(remoteSimpleEntity);

    SimpleEntity localEntityExpectedState = remoteSimpleEntity.clone();
    localEntityExpectedState.setString("Go ahead, Apollo");

    SimpleEntity localEntityNewState = remoteSimpleEntity.clone();
    localEntityNewState.setString("Crosstalk");

    TypedQuery<SimpleEntity> query = em.createQuery("SELECT se FROM SimpleEntity se", SimpleEntity.class);
    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from(em, query);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(new SyncRequestOperation<SimpleEntity>(Type.UPDATED, localEntityNewState, localEntityExpectedState));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = sds.coldSync(syncRequest);

    // ensure the response is as expected
    assertEquals(1, syncResponse.size());
    ConflictResponse<SimpleEntity> conflictResponse = (ConflictResponse<SimpleEntity>) syncResponse.get(0);
    assertEquals(remoteSimpleEntity.toString(), conflictResponse.getActualNew().toString());
    assertEquals(localEntityExpectedState.toString(), conflictResponse.getExpected().toString());
    assertEquals(localEntityNewState.toString(), conflictResponse.getRequestedNew().toString());
  }

  @Test
  public void testUpdateRemoteSideChanged() {
    SimpleEntity remoteSimpleEntity = new SimpleEntity();
    remoteSimpleEntity.setDate(new Timestamp(-2960391600000L));
    remoteSimpleEntity.setInteger(42);
    remoteSimpleEntity.setString("Mr. Watson--come here--I want to see you.");
    em.persist(remoteSimpleEntity);
    em.flush();

    SimpleEntity localSimpleEntity = remoteSimpleEntity.clone();

    remoteSimpleEntity.setString("This is different");
    em.flush();
    em.detach(remoteSimpleEntity);

    TypedQuery<SimpleEntity> query = em.createQuery("SELECT se FROM SimpleEntity se", SimpleEntity.class);
    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from(em, query);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.unchanged(localSimpleEntity));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = sds.coldSync(syncRequest);

    // ensure the response is as expected (nothing to do)
    assertEquals("Got unexpected response: " + syncResponse, 1, syncResponse.size());
    UpdateResponse<SimpleEntity> updateResponse = (UpdateResponse<SimpleEntity>) syncResponse.get(0);
    assertEquals(remoteSimpleEntity.toString(), updateResponse.getEntity().toString());
  }

  @Test
  public void testUpdateRequestingSideChanged() {
    SimpleEntity remoteSimpleEntity = new SimpleEntity();
    remoteSimpleEntity.setDate(new Timestamp(8917200000L));
    remoteSimpleEntity.setInteger(123456);
    remoteSimpleEntity.setString("Houston, we've had a problem.");
    em.persist(remoteSimpleEntity);
    em.flush();
    em.detach(remoteSimpleEntity);

    SimpleEntity localEntityExpectedState = remoteSimpleEntity.clone();

    SimpleEntity localEntityNewState = remoteSimpleEntity.clone();
    localEntityNewState.setString("No crosstalk");

    TypedQuery<SimpleEntity> query = em.createQuery("SELECT se FROM SimpleEntity se", SimpleEntity.class);
    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from(em, query);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(new SyncRequestOperation<SimpleEntity>(Type.UPDATED, localEntityNewState, localEntityExpectedState));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = sds.coldSync(syncRequest);

    // ensure the response is as expected
    assertEquals("Non-empty response: " + syncResponse, 0, syncResponse.size());
  }

  @Test
  public void testReceiveRemoteDelete() throws Exception {
    SimpleEntity localSimpleEntity = new SimpleEntity();
    localSimpleEntity.setDate(new Timestamp(-2960391600000L));
    localSimpleEntity.setInteger(42);
    localSimpleEntity.setString("Mr. Watson--come here--I want to see you.");

    TypedQuery<SimpleEntity> query = em.createQuery("SELECT se FROM SimpleEntity se", SimpleEntity.class);
    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from(em, query);

    // this sync request claims we were told in the past that the server has localSimpleEntity
    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.unchanged(localSimpleEntity));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = sds.coldSync(syncRequest);

    // ensure the response is as expected (the server doesn't have the entity anymore)
    assertEquals("Got unexpected response: " + syncResponse, 1, syncResponse.size());
    DeleteResponse<SimpleEntity> deleteResponse = (DeleteResponse<SimpleEntity>) syncResponse.get(0);
    assertEquals(localSimpleEntity.toString(), deleteResponse.getEntity().toString());
  }

  @Test
  public void testSendLocalDelete() throws Exception {
    // TODO
    fail("not implemented");
  }

  /**
   * This tests for the case where the client has generated its own ID locally,
   * and that ID is already in use on the server.
   */
  @Test
  public void testSendNewSimpleEntityThatHappensToHaveSameIdAsExistingRemoteEntity() {
    SimpleEntity unrelatedRemoteEntity = new SimpleEntity();
    unrelatedRemoteEntity.setString("Innocent bystander");
    unrelatedRemoteEntity.setDate(new Timestamp(System.currentTimeMillis()));
    unrelatedRemoteEntity.setInteger(2);
    em.persist(unrelatedRemoteEntity);
    em.flush();

    TypedQuery<SimpleEntity> query = em.createQuery("SELECT se FROM SimpleEntity se", SimpleEntity.class);
    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from(em, query);

    SimpleEntity localSimpleEntity = new SimpleEntity();
    SimpleEntity.setId(localSimpleEntity, unrelatedRemoteEntity.getId());
    localSimpleEntity.setDate(new Timestamp(System.currentTimeMillis()));
    localSimpleEntity.setInteger(1);
    localSimpleEntity.setString("Unwitting impostor");

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(new SyncRequestOperation<SimpleEntity>(SyncRequestOperation.Type.NEW, localSimpleEntity.clone(), null));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = sds.coldSync(syncRequest);

    // ensure the response is as expected
    assertEquals(2, syncResponse.size());
    IdChangeResponse<SimpleEntity> idChangeResponse = (IdChangeResponse<SimpleEntity>) syncResponse.get(0);
    assertEquals(unrelatedRemoteEntity.getId(), idChangeResponse.getOldId());
    Long newId = idChangeResponse.getEntity().getId(); // we will verify this in the next stanza

    // ensure the new entity actually got persisted on the server, and the innocent bystander is unharmed
    List<SimpleEntity> queryResult = em.createQuery("SELECT se FROM SimpleEntity se ORDER BY se.integer", SimpleEntity.class).getResultList();
    assertEquals(2, queryResult.size());
    SimpleEntity.setId(localSimpleEntity, newId); // set local instance's ID to the new remote one from the response
    assertEquals(localSimpleEntity.toString(), queryResult.get(0).toString());
    assertSame(unrelatedRemoteEntity, queryResult.get(1));
  }

}
