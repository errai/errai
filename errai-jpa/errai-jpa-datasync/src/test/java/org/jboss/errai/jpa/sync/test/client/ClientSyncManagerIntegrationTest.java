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

package org.jboss.errai.jpa.sync.test.client;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.CDIClientBootstrap;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.lifecycle.api.StateChange;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;
import org.jboss.errai.jpa.sync.client.shared.DataSyncService;
import org.jboss.errai.jpa.sync.client.shared.DeleteResponse;
import org.jboss.errai.jpa.sync.client.shared.IdChangeResponse;
import org.jboss.errai.jpa.sync.client.shared.NewRemoteEntityResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.jpa.sync.client.shared.UpdateResponse;
import org.jboss.errai.jpa.sync.test.client.entity.SimpleEntity;
import org.jboss.errai.jpa.sync.test.client.ioc.DependentScopedSyncBean;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Timer;

import junit.framework.AssertionFailedError;

public class ClientSyncManagerIntegrationTest extends GWTTestCase {

  private ClientSyncManager csm;
  private DependentScopedSyncBean syncBean;

  public native void setRemoteCommunicationEnabled(boolean enabled) /*-{
    $wnd.erraiBusRemoteCommunicationEnabled = enabled;
  }-*/;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.sync.test.DataSyncTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    setRemoteCommunicationEnabled(false);
    InitVotes.setTimeoutMillis(60000);

    ClientSyncManager.resetInstance();

    // Unfortunately, GWTTestCase does not call our inherited module's onModuleLoad() methods
    // http://code.google.com/p/google-web-toolkit/issues/detail?id=3791
    new CDI().__resetSubsystem();
    new Container().onModuleLoad();
    new CDIClientBootstrap().onModuleLoad();

    InitVotes.startInitPolling();

    super.gwtSetUp();

    csm = IOC.getBeanManager().lookupBean(ClientSyncManager.class).getInstance();

    csm.getDesiredStateEm().removeAll();
    csm.getExpectedStateEm().removeAll();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    assertFalse("ClientSyncManager 'sync in progress' flag got stuck on true", csm.isSyncInProgress());

    if (syncBean != null) {
      IOC.getBeanManager().destroyBean(syncBean);
    }

    Container.reset();
    IOC.reset();
    InitVotes.reset();
    setRemoteCommunicationEnabled(true);
    super.gwtTearDown();
  }

  public void testNewEntityFromServer() {
    final SimpleEntity newEntity = new SimpleEntity();
    newEntity.setString("the string value");
    newEntity.setDate(new Timestamp(1234567L));
    newEntity.setInteger(9999);
    SimpleEntity.setId(newEntity, 88L);

    final List<SyncRequestOperation<SimpleEntity>> expectedClientRequests =
        new ArrayList<>();
    // in this case, the client should make an empty request (both persistence contexts are empty)

    final List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<>();
    fakeServerResponses.add(new NewRemoteEntityResponse<>(newEntity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    final ErraiEntityManager esem = csm.getExpectedStateEm();
    final ErraiEntityManager dsem = csm.getDesiredStateEm();

    final SimpleEntity newEntityExpected = esem.find(SimpleEntity.class, newEntity.getId());
    final SimpleEntity newEntityDesired = dsem.find(SimpleEntity.class, newEntity.getId());
    assertEquals(newEntityExpected.toString(), newEntity.toString());
    assertEquals(newEntityDesired.toString(), newEntity.toString());
    assertNotSame("Expected State and Desired State instances must be separate", newEntityExpected, newEntityDesired);
  }

  public void testIdChangeFromServer() {
    final SimpleEntity entity = new SimpleEntity();
    entity.setString("the string value");
    entity.setDate(new Timestamp(1234567L));
    entity.setInteger(9999);

    final ErraiEntityManager esem = csm.getExpectedStateEm();
    final ErraiEntityManager dsem = csm.getDesiredStateEm();

    final SimpleEntity originalLocalState = dsem.merge(entity);
    final long originalId = originalLocalState.getId();
    dsem.flush();
    dsem.detach(originalLocalState);

    assertNull(esem.find(SimpleEntity.class, originalId));
    assertEquals(dsem.find(SimpleEntity.class, originalId).toString(), entity.toString());

    // Now change the ID and tell the ClientSyncManager it happened
    final long newId = originalLocalState.getId() + 100;
    SimpleEntity.setId(entity, newId);

    final List<SyncRequestOperation<SimpleEntity>> expectedClientRequests =
        new ArrayList<>();
    expectedClientRequests.add(SyncRequestOperation.created(originalLocalState));

    final List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<>();
    fakeServerResponses.add(new IdChangeResponse<>(originalId, entity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    assertNull(esem.find(SimpleEntity.class, originalId));
    assertNull(dsem.find(SimpleEntity.class, originalId));

    final SimpleEntity changedEntityExpected = esem.find(SimpleEntity.class, newId);
    final SimpleEntity changedEntityDesired = dsem.find(SimpleEntity.class, newId);
    assertEquals(changedEntityExpected.toString(), entity.toString());
    assertEquals(changedEntityDesired.toString(), entity.toString());
    assertNotSame(changedEntityDesired, changedEntityExpected);
  }

  // a hybrid of "new entity from server" and "id change from server"
  // the scenario is that we have a local entity with, say, ID 100
  // and the server tells us "here's a new entity. its ID is 100!"
  // so we have to move our existing entity out of the way before accepting the remote one.
  public void testNewEntityWithConflictingIdFromServer() {
    final SimpleEntity newRemote = new SimpleEntity();
    newRemote.setString("new entity from server");
    newRemote.setDate(new Timestamp(1234567L));
    newRemote.setInteger(9999);
    SimpleEntity.setId(newRemote, 100L);

    final SimpleEntity existingLocal = new SimpleEntity();
    existingLocal.setString("existing local entity");
    existingLocal.setDate(new Timestamp(7654321L));
    existingLocal.setInteger(8888);
    SimpleEntity.setId(existingLocal, 100L);

    final ErraiEntityManager esem = csm.getExpectedStateEm();
    final ErraiEntityManager dsem = csm.getDesiredStateEm();

    dsem.persist(existingLocal);
    dsem.flush();

    assertNull(esem.find(SimpleEntity.class, existingLocal.getId()));
    assertEquals(dsem.find(SimpleEntity.class, existingLocal.getId()).toString(), existingLocal.toString());

    final List<SyncRequestOperation<SimpleEntity>> expectedClientRequests =
        new ArrayList<>();
    expectedClientRequests.add(SyncRequestOperation.created(existingLocal)); // note that the mock
                                                                             // server will ignore
                                                                             // this for the purpose
                                                                             // of this test

    final List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<>();
    fakeServerResponses.add(new NewRemoteEntityResponse<>(newRemote));
    performColdSync(expectedClientRequests, fakeServerResponses);

    // now the ID of existingLocal (still managed by dsem) should not be 100 anymore

    assertFalse("Existing id " + existingLocal.getId() + " should not be the same as new object's ID "
        + newRemote.getId(),
            existingLocal.getId() == newRemote.getId());
    assertSame(existingLocal, dsem.find(SimpleEntity.class, existingLocal.getId()));

    assertEquals(newRemote.toString(), dsem.find(SimpleEntity.class, newRemote.getId()).toString());
    assertEquals(newRemote.toString(), esem.find(SimpleEntity.class, newRemote.getId()).toString());
  }

  public void testUpdateFromServer() {
    final SimpleEntity newEntity = new SimpleEntity();
    newEntity.setString("the string value");
    newEntity.setDate(new Timestamp(1234567L));
    newEntity.setInteger(9999);

    final ErraiEntityManager esem = csm.getExpectedStateEm();
    final ErraiEntityManager dsem = csm.getDesiredStateEm();

    // persist this as both the "expected state" from the server and the "desired state" on the
    // client
    final SimpleEntity originalEntityState = esem.merge(newEntity);
    esem.flush();
    esem.clear();

    dsem.persist(originalEntityState);
    dsem.flush();
    dsem.clear();

    final List<SyncRequestOperation<SimpleEntity>> expectedClientRequests =
        new ArrayList<>();
    expectedClientRequests.add(SyncRequestOperation.unchanged(originalEntityState));

    // now cook up a server response that says something changed
    SimpleEntity.setId(newEntity, originalEntityState.getId());
    newEntity.setString("a new string value");
    newEntity.setInteger(110011);
    final List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<>();
    fakeServerResponses.add(new UpdateResponse<>(newEntity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    final SimpleEntity changedEntityExpected = esem.find(SimpleEntity.class, newEntity.getId());
    final SimpleEntity changedEntityDesired = dsem.find(SimpleEntity.class, newEntity.getId());
    assertEquals(changedEntityExpected.toString(), newEntity.toString());
    assertEquals(changedEntityDesired.toString(), newEntity.toString());
    assertNotSame(changedEntityDesired, changedEntityExpected);
  }

  public void testDeleteFromServer() {
    final SimpleEntity newEntity = new SimpleEntity();
    newEntity.setString("the string value");
    newEntity.setDate(new Timestamp(1234567L));
    newEntity.setInteger(9999);

    final ErraiEntityManager esem = csm.getExpectedStateEm();
    final ErraiEntityManager dsem = csm.getDesiredStateEm();

    // persist this as both the "expected state" from the server and the "desired state" on the
    // client
    final SimpleEntity originalEntityState = esem.merge(newEntity);
    esem.flush();
    esem.clear();

    dsem.persist(originalEntityState);
    dsem.flush();
    dsem.clear();

    final List<SyncRequestOperation<SimpleEntity>> expectedClientRequests =
        new ArrayList<>();
    expectedClientRequests.add(SyncRequestOperation.unchanged(originalEntityState));

    // now cook up a server response that says it got deleted
    final List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<>();
    fakeServerResponses.add(new DeleteResponse<>(newEntity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    assertNull(esem.find(SimpleEntity.class, newEntity.getId()));
    assertNull(dsem.find(SimpleEntity.class, newEntity.getId()));

    // finally, ensure the deleted entity is not stuck in the REMOVED state
    // (should be NEW or DETACHED; we can verify by trying to merge it)
    try {
      esem.merge(newEntity);
    }
    catch (final IllegalArgumentException e) {
      fail("Merging removed entity failed: " + e);
    }

    try {
      dsem.merge(newEntity);
    }
    catch (final IllegalArgumentException e) {
      fail("Merging removed entity failed: " + e);
    }
  }

  public void testUpdateFromClient() {
    final SimpleEntity entity = new SimpleEntity();
    entity.setString("the string value");
    entity.setDate(new Timestamp(1234567L));
    entity.setInteger(9999);

    final ErraiEntityManager esem = csm.getExpectedStateEm();
    final ErraiEntityManager dsem = csm.getDesiredStateEm();

    // first persist the expected state
    final SimpleEntity originalEntityState = esem.merge(entity);
    esem.flush();
    esem.clear();

    // now make a change and persist the desired state
    SimpleEntity.setId(entity, originalEntityState.getId());
    entity.setString("this has been updated");

    dsem.persist(entity);
    dsem.flush();
    dsem.clear();

    final List<SyncRequestOperation<SimpleEntity>> expectedClientRequests =
        new ArrayList<>();
    expectedClientRequests.add(SyncRequestOperation.updated(entity, originalEntityState));

    // the server will respond with confirmation of the update
    final List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<>();
    fakeServerResponses.add(new UpdateResponse<>(entity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    final SimpleEntity changedEntityExpected = esem.find(SimpleEntity.class, entity.getId());
    final SimpleEntity changedEntityDesired = dsem.find(SimpleEntity.class, entity.getId());
    assertEquals(changedEntityExpected.toString(), entity.toString());
    assertEquals(changedEntityDesired.toString(), entity.toString());
    assertNotSame(changedEntityDesired, changedEntityExpected);
  }

  public void testDeleteFromClient() {
    final SimpleEntity entity = new SimpleEntity();
    entity.setString("the string value");
    entity.setDate(new Timestamp(1234567L));
    entity.setInteger(9999);

    final ErraiEntityManager esem = csm.getExpectedStateEm();
    final ErraiEntityManager dsem = csm.getDesiredStateEm();

    // first persist the expected state
    esem.persist(entity);
    esem.flush();
    esem.clear();

    final List<SyncRequestOperation<SimpleEntity>> expectedClientRequests =
        new ArrayList<>();
    expectedClientRequests.add(SyncRequestOperation.deleted(entity));

    // assuming no conflict, the server deletes the entity and generates the appropriate response
    final List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<>();
    fakeServerResponses.add(new DeleteResponse<>(entity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    assertNull(esem.find(SimpleEntity.class, entity.getId()));
    assertNull(dsem.find(SimpleEntity.class, entity.getId()));
  }

  public void testConcurrentSyncRequestsRejected() {
    final SimpleEntity entity = new SimpleEntity();
    entity.setString("the string value");
    entity.setDate(new Timestamp(1234567L));
    entity.setInteger(9999);

    final ErraiEntityManager esem = csm.getExpectedStateEm();
    final ErraiEntityManager dsem = csm.getDesiredStateEm();

    // first persist the desired state
    final SimpleEntity clientEntity = dsem.merge(entity);
    dsem.flush();
    dsem.clear();

    final Long originalId = clientEntity.getId();
    final List<SyncRequestOperation<SimpleEntity>> expectedClientRequests =
        new ArrayList<>();
    expectedClientRequests.add(SyncRequestOperation.created(clientEntity));

    // the server creates the entity with a different ID and notifies us of the change
    final List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<>();
    SimpleEntity.setId(entity, 1010L);
    fakeServerResponses.add(new IdChangeResponse<>(originalId, entity));

    final Runnable doDuringSync = new Runnable() {

      boolean alreadyRunning = false;

      @Override
      public void run() {
        if (alreadyRunning) {
          fail("Detected recursive call to coldSync()");
        }
        alreadyRunning = true;

        // at this point, ClientSyncManager is in the middle of a coldSync call. for safety, it is
        // required to fail.
        try {
          csm.coldSync("allSimpleEntities", SimpleEntity.class, Collections.<String, Object> emptyMap(),
                  new RemoteCallback<List<SyncResponse<SimpleEntity>>>() {
                    @Override
                    public void callback(final List<SyncResponse<SimpleEntity>> response) {
                      fail("this recursive call to coldSync must not succeed");
                    }
                  },
              new ErrorCallback<List<SyncResponse<SimpleEntity>>>() {
                @Override
                public boolean error(final List<SyncResponse<SimpleEntity>> message, final Throwable throwable) {
                  fail("this recursive call to coldSync should have failed synchronously");
                  throw new AssertionError();
                }
              });
          fail("recursive call to coldSync() failed to throw an exception");
        }
        catch (final IllegalStateException ex) {
          System.out
              .println("Got expected IllegalStateException. Returning normally so client state assertions can run.");
          // expected
        }
      }
    };

    performColdSync(expectedClientRequests, fakeServerResponses, doDuringSync);

    // now ensure the results of the original sync request were not harmed
    assertFalse(csm.isSyncInProgress());
    assertEquals(esem.find(SimpleEntity.class, entity.getId()).toString(), entity.toString());
    assertEquals(dsem.find(SimpleEntity.class, entity.getId()).toString(), entity.toString());
    assertNull(esem.find(SimpleEntity.class, originalId));
    assertNull(dsem.find(SimpleEntity.class, originalId));
  }

  public void testDeclarativeSync() {
    delayTestFinish(45000);

    final List<SyncResponse<SimpleEntity>> expectedSyncResponses = new ArrayList<>();

    final Map<String, Object> parameters = new HashMap<>();

    // replace the caller so we can see what the SyncWorker asks its ClientSyncManager to do
    Factory.maybeUnwrapProxy(csm).dataSyncService = new Caller<DataSyncService>() {

      @Override
      public DataSyncService call(final RemoteCallback<?> callback) {
        return new DataSyncService() {

          @SuppressWarnings({ "unchecked", "rawtypes" })
          @Override
          public <X> List<SyncResponse<X>> coldSync(final SyncableDataSet<X> dataSet,
              final List<SyncRequestOperation<X>> actualClientRequests) {
            System.out.println("Short-circuiting DataSyncService call:");
            System.out.println("   dataSet = " + dataSet);
            System.out.println("   actualClientRequests = " + actualClientRequests);

            // Don't assert anything here! The timer we start later on will still fire if the test
            // fails at this point!
            parameters.putAll(dataSet.getParameters());

            final RemoteCallback rawRemoteCallback = callback;
            rawRemoteCallback.callback(expectedSyncResponses);

            return null; // this is the Caller stub. it doesn't return the value directly.
          }
        };
      }

      @Override
      public DataSyncService call(final RemoteCallback<?> callback, final ErrorCallback<?> errorCallback) {
        return call(callback);
      }

      @Override
      public DataSyncService call() {
        fail("Unexpected use of callback");
        return null; // NOTREACHED
      }
    };

    syncBean = IOC.getBeanManager().lookupBean(DependentScopedSyncBean.class).getInstance();

    new Timer() {
      @Override
      public void run() {
        assertEquals(1l, parameters.get("id"));
        assertEquals("test", parameters.get("string"));
        assertEquals("literalValue", parameters.get("literal"));

        // should get back the exact list of sync responses that we returned from our fake
        // Caller<DataSyncService> above
        assertNotNull(syncBean.getResponses());
        assertSame(expectedSyncResponses, syncBean.getResponses().getResponses());

        // we expect 2 sync tasks to have happened (one after a short delay in start() and the first
        // repeating one after 5s)
        assertEquals(2, syncBean.getCallbackCount());
        finishTest();
      }

    }.schedule(7000);
  }

  @SuppressWarnings("unchecked")
  public void testDeclarativeSyncAndFieldValueChanges() {
    delayTestFinish(45000);

    final List<SyncResponse<SimpleEntity>> expectedSyncResponses = new ArrayList<>();

    final Map<String, Object> parameters = new HashMap<>();

    // replace the caller so we can see what the SyncWorker asks its ClientSyncManager to do
    csm.dataSyncService = new Caller<DataSyncService>() {

      @Override
      public DataSyncService call(final RemoteCallback<?> callback) {
        return new DataSyncService() {

          @SuppressWarnings({ "rawtypes" })
          @Override
          public <X> List<SyncResponse<X>> coldSync(final SyncableDataSet<X> dataSet,
              final List<SyncRequestOperation<X>> actualClientRequests) {
            System.out.println("Short-circuiting DataSyncService call:");
            System.out.println("   dataSet = " + dataSet);
            System.out.println("   actualClientRequests = " + actualClientRequests);

            // Don't assert anything here! The timer we start later on will still fire if the test
            // fails at this point!
            parameters.putAll(dataSet.getParameters());

            final RemoteCallback rawRemoteCallback = callback;
            rawRemoteCallback.callback(expectedSyncResponses);

            return null; // this is the Caller stub. it doesn't return the value directly.
          }
        };
      }

      @Override
      public DataSyncService call(final RemoteCallback<?> callback, final ErrorCallback<?> errorCallback) {
        return call(callback);
      }

      @Override
      public DataSyncService call() {
        fail("Unexpected use of callback");
        return null; // NOTREACHED
      }
    };

    // Change the field values and fire IOC state change event so the sync worker can update its
    // query parameters
    syncBean = IOC.getBeanManager().lookupBean(DependentScopedSyncBean.class).getInstance();
    syncBean.setId(1337);
    syncBean.setName("changed");
    final StateChange<DependentScopedSyncBean> changeEvent = IOC.getBeanManager().lookupBean(StateChange.class).getInstance();
    changeEvent.fireAsync(syncBean);

    new Timer() {

      @Override
      public void run() {
        assertEquals(1337l, parameters.get("id"));
        assertEquals("changed", parameters.get("string"));
        assertEquals("literalValue", parameters.get("literal"));

        // should get back the exact list of sync responses that we returned from our fake
        // Caller<DataSyncService> above
        assertNotNull(syncBean.getResponses());
        assertSame(expectedSyncResponses, syncBean.getResponses().getResponses());
        // we expect 2 sync tasks to have happened (one after a short delay in start() and the first
        // repeating one after 5s)
        assertEquals(2, syncBean.getCallbackCount());
        finishTest();
      }

    }.schedule(7000);
  }

  public void testDestructionCallbackStopSyncWorker() {
    delayTestFinish(45000);

    final List<SyncResponse<SimpleEntity>> expectedSyncResponses = new ArrayList<>();

    // replace the caller so we can see what the SyncWorker asks its ClientSyncManager to do
    csm.dataSyncService = new Caller<DataSyncService>() {

      @Override
      public DataSyncService call(final RemoteCallback<?> callback) {
        return new DataSyncService() {

          @SuppressWarnings({ "unchecked", "rawtypes" })
          @Override
          public <X> List<SyncResponse<X>> coldSync(final SyncableDataSet<X> dataSet,
              final List<SyncRequestOperation<X>> actualClientRequests) {
            System.out.println("Short-circuiting DataSyncService call:");
            System.out.println("   dataSet = " + dataSet);
            System.out.println("   actualClientRequests = " + actualClientRequests);

            final RemoteCallback rawRemoteCallback = callback;
            rawRemoteCallback.callback(expectedSyncResponses);

            return null; // this is the Caller stub. it doesn't return the value directly.
          }
        };
      }

      @Override
      public DataSyncService call(final RemoteCallback<?> callback, final ErrorCallback<?> errorCallback) {
        return call(callback);
      }

      @Override
      public DataSyncService call() {
        fail("Unexpected use of callback");
        return null; // NOTREACHED
      }
    };

    syncBean = IOC.getBeanManager().lookupBean(DependentScopedSyncBean.class).getInstance();
    IOC.getBeanManager().destroyBean(syncBean);

    new Timer() {
      @Override
      public void run() {
        assertEquals(0, syncBean.getCallbackCount());
        syncBean = null;
        finishTest();
      }

    }.schedule(7000);
  }

  /**
   * Calls ClientSyncManager.coldSync() in a way that no actual server communication happens. The
   * given "fake" server response is returned immediately to the ClientSyncManager's callback
   * function.
   *
   * @param expectedClientRequests
   *          The list of requests that the ClientSyncManager is expected to produce, based on the
   *          current state of its Expected State EntityManager and its Desired State EntityManager.
   *          If the contents of this list do not match the list produced by the ClientSyncManager,
   *          this method will throw an {@link AssertionFailedError}.
   * @param fakeServerResponses
   *          The list of SyncResponse operations to feed back to ClientSyncManager. The
   *          ClientSyncManager will process this list as if it was returned by the server.
   */
  private <Y> void performColdSync(
          final List<SyncRequestOperation<Y>> expectedClientRequests,
          final List<SyncResponse<Y>> fakeServerResponses) {
    performColdSync(expectedClientRequests, fakeServerResponses, null);
  }

  /**
   * Calls ClientSyncManager.coldSync() in a way that no actual server communication happens. The
   * given "fake" server response is returned immediately to the ClientSyncManager's callback
   * function.
   *
   * @param expectedClientRequests
   *          The list of requests that the ClientSyncManager is expected to produce, based on the
   *          current state of its Expected State EntityManager and its Desired State EntityManager.
   *          If the contents of this list do not match the list produced by the ClientSyncManager,
   *          this method will throw an {@link AssertionFailedError}.
   * @param fakeServerResponses
   *          The list of SyncResponse operations to feed back to ClientSyncManager. The
   *          ClientSyncManager will process this list as if it was returned by the server.
   * @param doDuringSync
   *          If non-null, this runnable is executed after the sync request ops are checked against
   *          the expected ones, but before the fake responses are delivered to the client sync
   *          manager.
   */
  private <Y> void performColdSync(
            final List<SyncRequestOperation<Y>> expectedClientRequests,
            final List<SyncResponse<Y>> fakeServerResponses,
            final Runnable doDuringSync) {

    csm.dataSyncService = new Caller<DataSyncService>() {

      @Override
      public DataSyncService call(final RemoteCallback<?> callback) {
        return new DataSyncService() {

          @SuppressWarnings({ "unchecked", "rawtypes" })
          @Override
          public <X> List<SyncResponse<X>> coldSync(final SyncableDataSet<X> dataSet,
              final List<SyncRequestOperation<X>> actualClientRequests) {
            final List erasedExpectedClientRequests = expectedClientRequests;
            assertSyncRequestsEqual(erasedExpectedClientRequests, actualClientRequests);

            if (doDuringSync != null) {
              doDuringSync.run();
            }

            final RemoteCallback erasedCallback = callback;
            erasedCallback.callback(fakeServerResponses);
            return null;
          }
        };
      }

      @Override
      public DataSyncService call(final RemoteCallback<?> callback, final ErrorCallback<?> errorCallback) {
        return call(callback);
      }

      @Override
      public DataSyncService call() {
        fail("Unexpected use of callback");
        return null; // NOTREACHED
      }
    };
    System.out.println("Overrode DataSyncService in ClientSyncManager");

    csm.coldSync("allSimpleEntities", SimpleEntity.class, Collections.<String, Object> emptyMap(),
            new RemoteCallback<List<SyncResponse<SimpleEntity>>>() {
              @Override
              public void callback(final List<SyncResponse<SimpleEntity>> response) {
                System.out.println("Got sync callback");
              }
            }, null);
  }

  private static <X> void assertSyncRequestsEqual(
          final List<SyncRequestOperation<X>> expected, final List<SyncRequestOperation<X>> actual) {
    assertEquals(
            "List lengths differ. Expected: " + stringify(expected) + ", actual: " + stringify(actual),
            expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals("Ops differ at index " + i, stringify(expected.get(i)), stringify(actual.get(i)));
    }
  }

  private static <X> String stringify(final List<SyncRequestOperation<X>> ops) {
    final StringBuilder sb = new StringBuilder(500);
    for (final SyncRequestOperation<X> op : ops) {
      sb.append(stringify(op)).append(" ");
    }
    return sb.toString();
  }

  private static <X> String stringify(final SyncRequestOperation<X> op) {
    return "[" + op.getType() + ": expected=" + op.getExpectedState() + ", desired=" + op.getEntity() + "]";
  }

}
