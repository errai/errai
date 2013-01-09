package org.jboss.errai.bus.server;

import junit.framework.TestCase;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.jboss.errai.marshalling.server.MappingContextSingleton;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Mike Brock
 */
public class ClusteringTests extends TestCase {

  static {
    MappingContextSingleton.get();
  }

  public void testCluster() throws Exception {
    final ErraiService svcA = InVMBusUtil.startService(1);
    final ErraiService svcB = InVMBusUtil.startService(2);

    BusTestClient busTestClientA = new BusTestClient(svcA.getBus());
    busTestClientA.connect();
    BusTestClient busTestClientB = new BusTestClient(svcB.getBus());
    busTestClientB.connect();

    final CountDownLatch countDownLatch = new CountDownLatch(2);

    final Set<String> resultsSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    busTestClientA.subscribe("localTest", new MessageCallback() {
      @Override
      public void callback(Message message) {
        String val = "Client 1:" + message.get(String.class, MessageParts.Value);
        resultsSet.add(val);
        countDownLatch.countDown();
      }
    });

    busTestClientB.subscribe("localTest", new MessageCallback() {
      @Override
      public void callback(Message message) {
        String val = "Client 2:" + message.get(String.class, MessageParts.Value);
        resultsSet.add(val);
        countDownLatch.countDown();
      }
    });

    MessageBuilder.createMessage()
        .toSubject("localTest")
        .signalling()
        .withValue("MSG")
        .noErrorHandling().sendGlobalWith(svcA.getBus());

    assertTrue(countDownLatch.await(1, TimeUnit.SECONDS));
    assertTrue(resultsSet.contains("Client 1:MSG"));
    assertTrue(resultsSet.contains("Client 2:MSG"));
  }
}
