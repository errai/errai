package org.jboss.errai.jpa.sync.test.client;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.CDIClientBootstrap;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;
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

import com.google.gwt.junit.client.GWTTestCase;

public class ClientSyncManagerIntegrationTest extends GWTTestCase {

  private ClientSyncManager csm;

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

    // Unfortunately, GWTTestCase does not call our inherited module's onModuleLoad() methods
    // http://code.google.com/p/google-web-toolkit/issues/detail?id=3791
    new IOCBeanManagerLifecycle().resetBeanManager();
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
    InitVotes.reset();
    setRemoteCommunicationEnabled(true);
    super.gwtTearDown();
  }

  public void testNewEntityFromServer() {
    SimpleEntity newEntity = new SimpleEntity();
    newEntity.setString("the string value");
    newEntity.setDate(new Timestamp(1234567L));
    newEntity.setInteger(9999);
    SimpleEntity.setId(newEntity, 88L);

    List<SyncRequestOperation<SimpleEntity>> expectedClientRequests = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    // in this case, the client should make an empty request (both persistence contexts are empty)

    List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<SyncResponse<SimpleEntity>>();
    fakeServerResponses.add(new NewRemoteEntityResponse<SimpleEntity>(newEntity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    ErraiEntityManager esem = csm.getExpectedStateEm();
    ErraiEntityManager dsem = csm.getDesiredStateEm();

    SimpleEntity newEntityExpected = esem.find(SimpleEntity.class, newEntity.getId());
    SimpleEntity newEntityDesired = dsem.find(SimpleEntity.class, newEntity.getId());
    assertEquals(newEntityExpected.toString(), newEntity.toString());
    assertEquals(newEntityDesired.toString(), newEntity.toString());
    assertNotSame("Expected State and Desired State instances must be separate", newEntityExpected, newEntityDesired);
  }

  public void testIdChangeFromServer() {
    SimpleEntity entity = new SimpleEntity();
    entity.setString("the string value");
    entity.setDate(new Timestamp(1234567L));
    entity.setInteger(9999);

    ErraiEntityManager esem = csm.getExpectedStateEm();
    ErraiEntityManager dsem = csm.getDesiredStateEm();

    SimpleEntity originalLocalState = dsem.merge(entity);
    long originalId = originalLocalState.getId();
    dsem.flush();
    dsem.detach(originalLocalState);

    assertNull(esem.find(SimpleEntity.class, originalId));
    assertEquals(dsem.find(SimpleEntity.class, originalId).toString(), entity.toString());

    // Now change the ID and tell the ClientSyncManager it happened
    long newId = originalLocalState.getId() + 100;
    SimpleEntity.setId(entity, newId);

    List<SyncRequestOperation<SimpleEntity>> expectedClientRequests = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    expectedClientRequests.add(SyncRequestOperation.created(originalLocalState));

    List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<SyncResponse<SimpleEntity>>();
    fakeServerResponses.add(new IdChangeResponse<SimpleEntity>(originalId, entity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    assertNull(esem.find(SimpleEntity.class, originalId));
    assertNull(dsem.find(SimpleEntity.class, originalId));

    SimpleEntity changedEntityExpected = esem.find(SimpleEntity.class, newId);
    SimpleEntity changedEntityDesired = dsem.find(SimpleEntity.class, newId);
    assertEquals(changedEntityExpected.toString(), entity.toString());
    assertEquals(changedEntityDesired.toString(), entity.toString());
    assertNotSame(changedEntityDesired, changedEntityExpected);
  }

  public void testUpdateFromServer() {
    SimpleEntity newEntity = new SimpleEntity();
    newEntity.setString("the string value");
    newEntity.setDate(new Timestamp(1234567L));
    newEntity.setInteger(9999);

    ErraiEntityManager esem = csm.getExpectedStateEm();
    ErraiEntityManager dsem = csm.getDesiredStateEm();

    // persist this as both the "expected state" from the server and the "desired state" on the client
    SimpleEntity originalEntityState = esem.merge(newEntity);
    esem.flush();
    esem.clear();

    dsem.persist(originalEntityState);
    dsem.flush();
    dsem.clear();

    List<SyncRequestOperation<SimpleEntity>> expectedClientRequests = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    expectedClientRequests.add(SyncRequestOperation.unchanged(originalEntityState));

    // now cook up a server response that says something changed
    SimpleEntity.setId(newEntity, originalEntityState.getId());
    newEntity.setString("a new string value");
    newEntity.setInteger(110011);
    List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<SyncResponse<SimpleEntity>>();
    fakeServerResponses.add(new UpdateResponse<SimpleEntity>(newEntity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    SimpleEntity changedEntityExpected = esem.find(SimpleEntity.class, newEntity.getId());
    SimpleEntity changedEntityDesired = dsem.find(SimpleEntity.class, newEntity.getId());
    assertEquals(changedEntityExpected.toString(), newEntity.toString());
    assertEquals(changedEntityDesired.toString(), newEntity.toString());
    assertNotSame(changedEntityDesired, changedEntityExpected);
  }

  public void testDeleteFromServer() {
    SimpleEntity newEntity = new SimpleEntity();
    newEntity.setString("the string value");
    newEntity.setDate(new Timestamp(1234567L));
    newEntity.setInteger(9999);

    ErraiEntityManager esem = csm.getExpectedStateEm();
    ErraiEntityManager dsem = csm.getDesiredStateEm();

    // persist this as both the "expected state" from the server and the "desired state" on the client
    SimpleEntity originalEntityState = esem.merge(newEntity);
    esem.flush();
    esem.clear();

    dsem.persist(originalEntityState);
    dsem.flush();
    dsem.clear();

    List<SyncRequestOperation<SimpleEntity>> expectedClientRequests = new ArrayList<SyncRequestOperation<SimpleEntity>>();
    expectedClientRequests.add(SyncRequestOperation.unchanged(originalEntityState));

    // now cook up a server response that says it got deleted
    List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<SyncResponse<SimpleEntity>>();
    fakeServerResponses.add(new DeleteResponse<SimpleEntity>(newEntity));
    performColdSync(expectedClientRequests, fakeServerResponses);

    assertNull(esem.find(SimpleEntity.class, newEntity.getId()));
    assertNull(dsem.find(SimpleEntity.class, newEntity.getId()));
  }

  public <Y> void performColdSync(
          final List<SyncRequestOperation<Y>> expectedClientRequests,
          final List<SyncResponse<Y>> fakeServerResponses) {

    csm.dataSyncService = new Caller<DataSyncService>() {

      @Override
      public DataSyncService call(final RemoteCallback<?> callback) {
        return new DataSyncService() {

          @SuppressWarnings({"unchecked", "rawtypes"})
          @Override
          public <X> List<SyncResponse<X>> coldSync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> actualClientRequests) {
            List erasedExpectedClientRequests = expectedClientRequests;
            assertSyncRequestsEqual(erasedExpectedClientRequests, actualClientRequests);
            RemoteCallback erasedCallback = callback;
            erasedCallback.callback(fakeServerResponses);
            return null;
          }
        };
      }

      @Override
      public DataSyncService call(final RemoteCallback<?> callback, final ErrorCallback<?> errorCallback) {
        fail("Unexpected use of callback");
        return null; // NOTREACHED
      }

      @Override
      public DataSyncService call() {
        fail("Unexpected use of callback");
        return null; // NOTREACHED
      }
    };
    System.out.println("Overrode DataSyncService in ClientSyncManager");

    csm.coldSync("allSimpleEntities", SimpleEntity.class, Collections.<String,Object>emptyMap());
  }

  private static <X> void assertSyncRequestsEqual(
          List<SyncRequestOperation<X>> expected, List<SyncRequestOperation<X>> actual) {
    assertEquals(
            "List lengths differ. Expected: " + stringify(expected) + ", actual: " + stringify(actual),
            expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals("Ops differ at index " + i, stringify(expected.get(i)), stringify(actual.get(i)));
    }
  }

  private static <X> String stringify(List<SyncRequestOperation<X>> ops) {
    StringBuilder sb = new StringBuilder(500);
    for (SyncRequestOperation<X> op : ops) {
      sb.append(stringify(op)).append(" ");
    }
    return sb.toString();
  }

  private static <X> String stringify(SyncRequestOperation<X> op) {
    return "[" + op.getType() + ": expected=" + op.getExpectedState() + ", desired=" + op.getEntity() + "]";
  }

}
