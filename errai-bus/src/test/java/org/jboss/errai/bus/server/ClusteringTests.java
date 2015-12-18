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

package org.jboss.errai.bus.server;

import junit.framework.TestCase;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.server.MappingContextSingleton;

import java.util.ArrayList;
import java.util.Collections;
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

  @Override
  protected void setUp() throws Exception {
    MappingContextSingleton.get();
  }

  private static class LatchCounter implements Runnable {
    private final CountDownLatch latch;

    private LatchCounter(CountDownLatch latch) {
      this.latch = latch;
    }

    @Override
    public void run() {
      latch.countDown();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    for (final ErraiService svc : startedInstances) {
      svc.stopService();
    }

    startedInstances.clear();
  }

  public void testGlobalMessageInCluster() throws Exception {
    final ErraiService nodeA = startInstance();
    final ErraiService nodeB = startInstance();

    final BusTestClient clientA = BusTestClient.create(nodeA);
    final BusTestClient clientB = BusTestClient.create(nodeB);

    final CountDownLatch initLatch = new CountDownLatch(2);
    clientA.addInitCallback(new LatchCounter(initLatch));
    clientB.addInitCallback(new LatchCounter(initLatch));

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

    // connect only after all services are subscribed.
    clientA.connect();
    clientB.connect();

    initLatch.await(5, TimeUnit.SECONDS);

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

    final BusTestClient clientA = BusTestClient.create(serverA);
    final BusTestClient clientB = BusTestClient.create(serverB);

    final CountDownLatch initLatch = new CountDownLatch(2);
    clientA.addInitCallback(new LatchCounter(initLatch));
    clientB.addInitCallback(new LatchCounter(initLatch));

    final QueueSession clientASession = clientA.getServerSession();
    final QueueSession clientBSession = clientB.getServerSession();

    final Set<String> resultsSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    final CountDownLatch countDownLatch = new CountDownLatch(2);

    final String localService = "localTest";

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

    // connect only after all services are subscribed.
    clientA.connect();
    clientB.connect();

    initLatch.await(5, TimeUnit.SECONDS);

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

    final BusTestClient clientB = BusTestClient.create(serverB);

    final CountDownLatch initLatch = new CountDownLatch(1);
    clientB.addInitCallback(new LatchCounter(initLatch));

    final QueueSession clientBSession = clientB.getServerSession();

    final String localService = "localTest";

    final Set<String> resultsSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    final CountDownLatch firstLatch = new CountDownLatch(1);

    class StateHolder {
      private String name;
      private CountDownLatch latch;
    }

    final StateHolder holder = new StateHolder();
    holder.name = "ClientB1";

    // set the latch which will be used in the callback.
    holder.latch = firstLatch;

    final MessageCallback receiver = new MessageCallback() {
      @Override
      public void callback(final Message message) {
        final String e = holder.name + ":" + message.getValue(String.class);
        System.out.println("result:" + e);
        resultsSet.add(e);
        holder.latch.countDown();
      }
    };

    clientB.subscribe(localService, receiver);

    // connect only after all services are subscribed.
    clientB.connect();

    initLatch.await(5, TimeUnit.SECONDS);

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerA")
        .with(MessageParts.SessionID, clientBSession.getSessionId())
        .noErrorHandling()
        .sendNowWith(serverA.getBus());

    firstLatch.await(5, TimeUnit.SECONDS);

    final CountDownLatch secondLatch = new CountDownLatch(1);
    // replace the reference in the holder so the callback now uses this latch.
    holder.latch = secondLatch;

    clientB.clearInitCallbacks();

    final CountDownLatch changeOverLatch = new CountDownLatch(1);

    clientB.addInitCallback(new Runnable() {
      @Override
      public void run() {
        changeOverLatch.countDown();
      }
    });

    /**
     * Move ClientB to ServerC.
     */
    clientB.changeBus(serverC);

    changeOverLatch.await(5, TimeUnit.SECONDS);
    holder.name = "ClientB2";

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerA")
        .with(MessageParts.SessionID, clientBSession.getSessionId())
        .noErrorHandling()
        .sendNowWith(serverA.getBus());

    assertTrue("timed out waiting for results", secondLatch.await(5, TimeUnit.SECONDS));
    assertTrue("expected result missing", resultsSet.contains("ClientB1:ServerA"));
    assertTrue("expected result missing", resultsSet.contains("ClientB2:ServerA"));
  }
}
