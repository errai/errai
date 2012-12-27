package org.jboss.errai.bus.server;

import junit.framework.TestCase;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.server.service.ErraiService;

/**
 * @author Mike Brock
 */
public class ClusteringTests extends TestCase {

  public void testCluster() {
    final ErraiService svcA = InVMBusUtil.startService(1);
    final ErraiService svcB = InVMBusUtil.startService(2);

    svcA.getBus().subscribe("test", new MessageCallback() {
      @Override
      public void callback(final Message message) {
        System.out.println("YAY!");
      }
    });

    svcB.getBus().subscribe("test", new MessageCallback() {
      @Override
      public void callback(final Message message) {
        System.out.println("YAY2!");
      }
    });

    BusTestClient busTestClientA = new BusTestClient(svcA.getBus());
    busTestClientA.connect();

    try {
      Thread.sleep(5000);
    }
    catch (Throwable t) {
    }
  }


}
