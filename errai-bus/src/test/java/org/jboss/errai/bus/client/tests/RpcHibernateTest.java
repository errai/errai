package org.jboss.errai.bus.client.tests;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.tests.support.HibernateObject;
import org.jboss.errai.bus.client.tests.support.HibernateRpc;
import org.jboss.errai.bus.client.tests.support.OtherHibernateObject;
import org.jboss.errai.common.client.api.RemoteCallback;

public class RpcHibernateTest extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testRpcWithReturnValFromHibernate() throws Exception {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final BusErrorCallback errorCallback = new BusErrorCallback() {
          @Override
          public boolean error(Message message, Throwable throwable) {
            // This will cause the test to timeout if the inner call has an
            // error.
            if (throwable != null)
              throwable.printStackTrace();
            fail();

            return false;
          }
        };
        MessageBuilder.createCall(new RemoteCallback<Void>() {

          @Override
          public void callback(Void response) {
            MessageBuilder.createCall(new RemoteCallback<OtherHibernateObject>() {
              @Override
              public void callback(OtherHibernateObject response) {
                finishTest();
              }
            }, errorCallback, HibernateRpc.class).getOther(1);

          }
        }, errorCallback, HibernateRpc.class).addHibernateObject(new HibernateObject(1, new OtherHibernateObject()));
      }
    });
  }

}
