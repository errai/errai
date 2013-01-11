package org.jboss.errai.bus.server;

import junit.framework.TestCase;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.jboss.errai.marshalling.server.MappingContextSingleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Brock
 */
public class ClusteringTests extends TestCase {
  private final List<ErraiService> startedInstances = new ArrayList<ErraiService>();
  private final AtomicInteger counter = new AtomicInteger(0);

  private ErraiService startInstance() {
    final ErraiService newService = InVMBusUtil.startService(counter.incrementAndGet());
    startedInstances.add(newService);
    return newService;
  }

  static {
    MappingContextSingleton.get();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    for (final ErraiService svc : startedInstances) {
      svc.stopService();
    }

    startedInstances.clear();

    System.gc();
  }

  public void testGlobalMessageInCluster() throws Exception {
    final ErraiService nodeA = startInstance();
    final ErraiService nodeB = startInstance();

    final MessageBus clientA = BusTestClient.connect(nodeA);
    final MessageBus clientB = BusTestClient.connect(nodeB);

    final CountDownLatch countDownLatch = new CountDownLatch(2);

    final Set<String> resultsSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    final String localService = "localTest";

    clientA.subscribe(localService, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        final String val = "Client 1:" + message.get(String.class, MessageParts.Value);
        if (resultsSet.add(val)) {
          countDownLatch.countDown();
        }
        else {
          System.out.println("WARNING: Received dup!");
        }
      }
    });

    clientB.subscribe(localService, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        final String val = "Client 2:" + message.get(String.class, MessageParts.Value);
        if (resultsSet.add(val)) {
          countDownLatch.countDown();
        }
        else {
          System.out.println("WARNING: Received dup!");
        }
      }
    });

    Thread.sleep(500);

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("MSG")
        .noErrorHandling().sendGlobalWith(nodeA.getBus());

    assertTrue(countDownLatch.await(10, TimeUnit.SECONDS));
    assertTrue(resultsSet.contains("Client 1:MSG"));
    assertTrue(resultsSet.contains("Client 2:MSG"));
  }

  public void testPointToPointMessageAcrossClusterNodes() throws Exception {
    final ErraiService serverA = startInstance();
    final ErraiService serverB = startInstance();

    final BusTestClient clientA = BusTestClient.connect(serverA);
    final BusTestClient clientB = BusTestClient.connect(serverB);

    final QueueSession clientASession = clientA.getServerSession();
    final QueueSession clientBSession = clientB.getServerSession();

    final String localService = "localTest";

    final Set<String> resultsSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    final CountDownLatch countDownLatch = new CountDownLatch(2);

    clientA.subscribe(localService, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        resultsSet.add("ClientA:" + message.getValue(String.class));
        countDownLatch.countDown();
      }
    });

    clientB.subscribe(localService, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        resultsSet.add("ClientB:" + message.getValue(String.class));
        countDownLatch.countDown();
      }
    });

    Thread.sleep(500);

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerB")
        .with(MessageParts.SessionID, clientASession.getSessionId())
        .noErrorHandling()
        .sendNowWith(serverB.getBus());

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerA")
        .with(MessageParts.SessionID, clientBSession.getSessionId())
        .noErrorHandling()
        .sendNowWith(serverA.getBus());

    assertTrue("timed out waiting for results", countDownLatch.await(60, TimeUnit.SECONDS));
    assertTrue("expected result missing", resultsSet.contains("ClientA:ServerB"));
    assertTrue("expected result missing", resultsSet.contains("ClientB:ServerA"));
  }

  public void testSessionMovesAfterBeingCached() throws Exception {
    final ErraiService serverA = startInstance();
    final ErraiService serverB = startInstance();
    final ErraiService serverC = startInstance();

    final BusTestClient clientA = BusTestClient.connect(serverA);
    final BusTestClient clientB = BusTestClient.connect(serverB);

    final QueueSession clientBSession = clientB.getServerSession();

    final String localService = "localTest";

    final Set<String> resultsSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    final CountDownLatch countDownLatch = new CountDownLatch(2);

    class NameHolder {
      private String name;
    }

    final NameHolder clientName = new NameHolder();
    clientName.name = "ClientB1";

    clientB.subscribe(localService, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        System.out.println("**received**");
        resultsSet.add(clientName.name + ":" + message.getValue(String.class));
        countDownLatch.countDown();
      }
    });


    Thread.sleep(1000);

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerA")
        .with(MessageParts.SessionID, clientBSession.getSessionId())
        .noErrorHandling()
        .sendNowWith(serverA.getBus());

    Thread.sleep(1000);

    /**
     * Move ClientB to ServerC.
     */
    clientB.changeBus(serverC);

    Thread.sleep(1000);

    clientName.name = "ClientB2";

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerA")
        .with(MessageParts.SessionID, clientBSession.getSessionId())
        .noErrorHandling()
        .sendNowWith(serverA.getBus());

    assertTrue("timed out waiting for results", countDownLatch.await(60, TimeUnit.SECONDS));
    assertTrue("expected result missing", resultsSet.contains("ClientB1:ServerA"));
    assertTrue("expected result missing", resultsSet.contains("ClientB2:ServerA"));
  }
}
