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

    List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<SyncResponse<SimpleEntity>>();
    fakeServerResponses.add(new NewRemoteEntityResponse<SimpleEntity>(newEntity));
    feedResponseToClientSyncManager(fakeServerResponses);

    ErraiEntityManager esem = csm.getExpectedStateEm();
    ErraiEntityManager dsem = csm.getDesiredStateEm();

    SimpleEntity newEntityExpected = esem.find(SimpleEntity.class, newEntity.getId());
    SimpleEntity newEntityDesired = dsem.find(SimpleEntity.class, newEntity.getId());
    assertEquals(newEntityExpected.toString(), newEntity.toString());
    assertEquals(newEntityDesired.toString(), newEntity.toString());
    assertNotSame("Expected State and Desired State instances must be separate", newEntityExpected, newEntityDesired);
  }

  public void testIdChange() {
    SimpleEntity newEntity = new SimpleEntity();
    newEntity.setString("the string value");
    newEntity.setDate(new Timestamp(1234567L));
    newEntity.setInteger(9999);

    ErraiEntityManager esem = csm.getExpectedStateEm();
    ErraiEntityManager dsem = csm.getDesiredStateEm();

    dsem.persist(newEntity);
    long originalId = newEntity.getId();
    dsem.flush();
    dsem.detach(newEntity);

    assertNull(esem.find(SimpleEntity.class, originalId));
    assertEquals(dsem.find(SimpleEntity.class, originalId).toString(), newEntity.toString());

    // Now change the ID and tell the ClientSyncManager it happened
    long newId = newEntity.getId() + 100;
    SimpleEntity.setId(newEntity, newId);

    List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<SyncResponse<SimpleEntity>>();
    fakeServerResponses.add(new IdChangeResponse<SimpleEntity>(originalId, newEntity));
    feedResponseToClientSyncManager(fakeServerResponses);

    assertNull(esem.find(SimpleEntity.class, originalId));
    assertNull(dsem.find(SimpleEntity.class, originalId));

    SimpleEntity changedEntityExpected = esem.find(SimpleEntity.class, newId);
    SimpleEntity changedEntityDesired = dsem.find(SimpleEntity.class, newId);
    assertEquals(changedEntityExpected.toString(), newEntity.toString());
    assertEquals(changedEntityDesired.toString(), newEntity.toString());
    assertNotSame(changedEntityDesired, changedEntityExpected);
  }

  public void testUpdateExistingEntity() {
    SimpleEntity newEntity = new SimpleEntity();
    newEntity.setString("the string value");
    newEntity.setDate(new Timestamp(1234567L));
    newEntity.setInteger(9999);

    ErraiEntityManager esem = csm.getExpectedStateEm();
    ErraiEntityManager dsem = csm.getDesiredStateEm();

    // persist this as both the "expected state" from the server and the "desired state" on the client
    esem.persist(newEntity);
    esem.flush();
    esem.detach(newEntity);

    dsem.persist(newEntity);
    dsem.flush();
    dsem.detach(newEntity);

    // now cook up a server response that says something changed
    newEntity.setString("a new string value");
    newEntity.setInteger(110011);
    List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<SyncResponse<SimpleEntity>>();
    fakeServerResponses.add(new UpdateResponse<SimpleEntity>(newEntity));
    feedResponseToClientSyncManager(fakeServerResponses);

    SimpleEntity changedEntityExpected = esem.find(SimpleEntity.class, newEntity.getId());
    SimpleEntity changedEntityDesired = dsem.find(SimpleEntity.class, newEntity.getId());
    assertEquals(changedEntityExpected.toString(), newEntity.toString());
    assertEquals(changedEntityDesired.toString(), newEntity.toString());
    assertNotSame(changedEntityDesired, changedEntityExpected);
  }

  public void testDeleteExistingEntity() {
    SimpleEntity newEntity = new SimpleEntity();
    newEntity.setString("the string value");
    newEntity.setDate(new Timestamp(1234567L));
    newEntity.setInteger(9999);

    ErraiEntityManager esem = csm.getExpectedStateEm();
    ErraiEntityManager dsem = csm.getDesiredStateEm();

    // persist this as both the "expected state" from the server and the "desired state" on the client
    esem.persist(newEntity);
    esem.flush();
    esem.detach(newEntity);

    dsem.persist(newEntity);
    dsem.flush();
    dsem.detach(newEntity);

    // now cook up a server response that says it got deleted
    List<SyncResponse<SimpleEntity>> fakeServerResponses = new ArrayList<SyncResponse<SimpleEntity>>();
    fakeServerResponses.add(new DeleteResponse<SimpleEntity>(newEntity));
    feedResponseToClientSyncManager(fakeServerResponses);

    assertNull(esem.find(SimpleEntity.class, newEntity.getId()));
    assertNull(dsem.find(SimpleEntity.class, newEntity.getId()));
  }

  public <Y> void feedResponseToClientSyncManager(final List<SyncResponse<Y>> fakeServerResponses) {
    csm.dataSyncService = new Caller<DataSyncService>() {

      @Override
      public DataSyncService call(final RemoteCallback<?> callback) {
        return new DataSyncService() {

          @SuppressWarnings({"unchecked", "rawtypes"})
          @Override
          public <X> List<SyncResponse<X>> coldSync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> remoteResults) {
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
}
