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

package org.jboss.errai.jpa.sync.test.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.jboss.errai.jpa.sync.client.shared.ConflictResponse;
import org.jboss.errai.jpa.sync.client.shared.DeleteResponse;
import org.jboss.errai.jpa.sync.client.shared.IdChangeResponse;
import org.jboss.errai.jpa.sync.client.shared.NewRemoteEntityResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.jpa.sync.client.shared.UpdateResponse;
import org.jboss.errai.jpa.sync.server.DataSyncServiceImpl;
import org.jboss.errai.jpa.sync.server.JavaReflectionAttributeAccessor;
import org.jboss.errai.jpa.sync.test.client.entity.SimpleEntity;
import org.junit.Before;
import org.junit.Test;


public class DataSyncServiceUnitTest extends AbstractServerSideDataSyncTest {

  private final Map<String, Object> NO_PARAMS = Collections.emptyMap();

  private DataSyncServiceImpl dss;

  @Before
  public void setupDss() {
    dss = new DataSyncServiceImpl(em, new JavaReflectionAttributeAccessor());
  }

  @Test
  public void testSendNewSimpleEntityNoConflict() {
    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    SimpleEntity localSimpleEntity = new SimpleEntity();
    SimpleEntity.setId(localSimpleEntity, 1234L); // simulating an ID we generated in the browser; Hibernate doesn't know about it
    localSimpleEntity.setDate(new Timestamp(System.currentTimeMillis()));
    localSimpleEntity.setInteger(42);
    localSimpleEntity.setString("This was recorded on sticky tape and rust.");

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.created(new SimpleEntity(localSimpleEntity)));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

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

    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();

    // now do the actual sync (we're starting from empty on the local (requesting) side)
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

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

    SimpleEntity localSimpleEntity = new SimpleEntity(remoteSimpleEntity);

    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.unchanged(localSimpleEntity));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

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

    SimpleEntity localEntityExpectedState = new SimpleEntity(remoteSimpleEntity);
    localEntityExpectedState.setString("Go ahead, Apollo");

    SimpleEntity localEntityNewState = new SimpleEntity(remoteSimpleEntity);
    localEntityNewState.setString("Crosstalk");

    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.updated(localEntityNewState, localEntityExpectedState));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

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

    SimpleEntity localSimpleEntity = new SimpleEntity(remoteSimpleEntity);

    remoteSimpleEntity.setString("This is different");
    em.flush();
    em.detach(remoteSimpleEntity);

    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.unchanged(localSimpleEntity));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

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

    SimpleEntity localEntityExpectedState = new SimpleEntity(remoteSimpleEntity);

    SimpleEntity localEntityNewState = new SimpleEntity(remoteSimpleEntity);
    localEntityNewState.setString("No crosstalk");

    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.updated(localEntityNewState, localEntityExpectedState));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

    // ensure the response is as expected (ack of the update)
    assertEquals("Got unexpected response: " + syncResponse, 1, syncResponse.size());
    UpdateResponse<SimpleEntity> updateResponse = (UpdateResponse<SimpleEntity>) syncResponse.get(0);
    assertEquals(localEntityNewState.toString(), updateResponse.getEntity().toString());
    assertEquals(localEntityExpectedState.getVersion() + 1, updateResponse.getEntity().getVersion());
  }

  @Test
  public void testReceiveRemoteDelete() throws Exception {
    SimpleEntity localSimpleEntity = new SimpleEntity();
    localSimpleEntity.setDate(new Timestamp(-2960391600000L));
    localSimpleEntity.setInteger(42);
    localSimpleEntity.setString("Mr. Watson--come here--I want to see you.");
    SimpleEntity.setId(localSimpleEntity, 123L);

    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    // this sync request claims we were told in the past that the server has localSimpleEntity
    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.unchanged(localSimpleEntity));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

    // ensure the response is as expected (the server doesn't have the entity anymore)
    assertEquals("Got unexpected response: " + syncResponse, 1, syncResponse.size());
    DeleteResponse<SimpleEntity> deleteResponse = (DeleteResponse<SimpleEntity>) syncResponse.get(0);
    assertEquals(localSimpleEntity.toString(), deleteResponse.getEntity().toString());
  }

  @Test
  public void testSendLocalDelete() throws Exception {
    SimpleEntity remoteSimpleEntity = new SimpleEntity();
    remoteSimpleEntity.setDate(new Timestamp(123456700000L));
    remoteSimpleEntity.setInteger(9);
    remoteSimpleEntity.setString("You will be terminated");
    em.persist(remoteSimpleEntity);
    em.flush();
    em.detach(remoteSimpleEntity);

    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    // this sync request claims we were told in the past that the server has localSimpleEntity
    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.deleted(remoteSimpleEntity));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

    // the server's response should acknowledge the delete operation
    assertEquals("Got unexpected response: " + syncResponse, 1, syncResponse.size());
    DeleteResponse<SimpleEntity> deleteResponse = (DeleteResponse<SimpleEntity>) syncResponse.get(0);
    assertEquals(remoteSimpleEntity.toString(), deleteResponse.getEntity().toString());

    // verify the server deleted the entity
    TypedQuery<SimpleEntity> query = em.createNamedQuery("allSimpleEntities", SimpleEntity.class);
    List<SimpleEntity> newQueryResult = query.getResultList();
    assertTrue("Uh-oh! Entity should have been deleted! " + newQueryResult, newQueryResult.isEmpty());
  }

  /**
   * Tests that it is harmless to say we deleted an entity that's already gone
   * on the server.
   */
  @Test
  public void testSendLocalDeleteForRemotelyDeletedEntity() throws Exception {
    SimpleEntity remoteSimpleEntity = new SimpleEntity();
    remoteSimpleEntity.setDate(new Timestamp(123456700000L));
    remoteSimpleEntity.setInteger(9);
    remoteSimpleEntity.setString("You will be terminated");

    // ensure it gets an ID assigned
    em.persist(remoteSimpleEntity);
    em.flush();

    // now delete it
    em.remove(remoteSimpleEntity);
    em.flush();
    em.detach(remoteSimpleEntity);

    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    // this sync request claims we were told in the past that the server has localSimpleEntity
    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.deleted(remoteSimpleEntity));

    // now do the actual sync (which should have no effect, since remote entity is already gone)
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

    // ensure the response is empty, as expected
    assertEquals("Got unexpected response: " + syncResponse, 0, syncResponse.size());
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

    SyncableDataSet<SimpleEntity> sds = SyncableDataSet.from("allSimpleEntities", SimpleEntity.class, NO_PARAMS);

    SimpleEntity localSimpleEntity = new SimpleEntity();
    SimpleEntity.setId(localSimpleEntity, unrelatedRemoteEntity.getId());
    localSimpleEntity.setDate(new Timestamp(System.currentTimeMillis()));
    localSimpleEntity.setInteger(1);
    localSimpleEntity.setString("Unwitting impostor");

    List<SyncRequestOperation<SimpleEntity>> syncRequest = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    syncRequest.add(SyncRequestOperation.created(new SimpleEntity(localSimpleEntity)));

    // now do the actual sync
    List<SyncResponse<SimpleEntity>> syncResponse = dss.coldSync(sds, syncRequest);

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
