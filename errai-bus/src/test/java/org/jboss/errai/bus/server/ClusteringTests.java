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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.framework.BuiltInServices;
import org.jboss.errai.bus.client.protocols.BusCommand;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.io.MessageDeliveryHandler;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.server.MappingContextSingleton;

import junit.framework.TestCase;

/**
 * @author Mike Brock
 */
public class ClusteringTests extends TestCase {
  private final List<ErraiService<?>> startedInstances = new ArrayList<>();
  private final AtomicInteger counter = new AtomicInteger(0);

  private ErraiService<?> startInstance() {
    final ErraiService<?> newService = InVMBusUtil.startService(counter.incrementAndGet());
    startedInstances.add(newService);
    return newService;
  }

  @Override
  protected void setUp() throws Exception {
    MappingContextSingleton.get();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    startedInstances.forEach(svc -> svc.stopService());
    startedInstances.clear();
  }

  public void testGlobalMessageInCluster() throws Exception {
    final ErraiService<?> nodeA = startInstance();
    final ErraiService<?> nodeB = startInstance();
    final QueueSession sessionA = MockQueueSessionFactory.newSession("client1");
    final QueueSession sessionB = MockQueueSessionFactory.newSession("client2");
    final QueueSession broadCastBlockingSession = MockQueueSessionFactory.newSession("dummy");

    associateQueueSessionToBus(sessionA, nodeA.getBus());
    associateQueueSessionToBus(sessionB, nodeB.getBus());
    // Prevents broadcasting of messages so that they can be intercepted
    associateQueueSessionToBus(broadCastBlockingSession, nodeA.getBus());
    associateQueueSessionToBus(broadCastBlockingSession, nodeB.getBus());

    final Set<String> resultsSet = new HashSet<>();
    final String localService = "localTest";
    final CountDownLatch latch = new CountDownLatch(2);

    remoteSubscibeToTopic(sessionA, nodeA.getBus(), localService);
    remoteSubscibeToTopic(sessionB, nodeB.getBus(), localService);

    mockTransportWithAction(nodeA.getBus(), sessionA, msg -> {
      if (localService.equals(msg.getSubject())) {
        final String val = "Client 1:" + msg.get(String.class, MessageParts.Value);
        if (resultsSet.add(val)) {
          latch.countDown();
        }
        else {
          fail("Received duplicate message to Client 1.");
        }
      }
    });

    mockTransportWithAction(nodeB.getBus(), sessionB, msg -> {
      if (localService.equals(msg.getSubject())) {
        final String val = "Client 2:" + msg.get(String.class, MessageParts.Value);
        if (resultsSet.add(val)) {
          latch.countDown();
        }
        else {
          fail("Received duplicate message to Client 2.");
        }
      }
    });

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("MSG")
        .noErrorHandling()
        .sendGlobalWith(nodeA.getBus());

    assertTrue("Timeout while waiting for messages from servers. Received: " + resultsSet, latch.await(30, TimeUnit.SECONDS));
    assertEquals(new HashSet<>(Arrays.asList("Client 1:MSG", "Client 2:MSG")), resultsSet);
  }

  public void testPointToPointMessageInCluster() throws Exception {
    final ErraiService<?> nodeA = startInstance();
    final ErraiService<?> nodeB = startInstance();
    final QueueSession sessionA = MockQueueSessionFactory.newSession("client1");
    final QueueSession sessionB = MockQueueSessionFactory.newSession("client2");
    final QueueSession broadCastBlockingSession = MockQueueSessionFactory.newSession("dummy");

    associateQueueSessionToBus(sessionA, nodeA.getBus());
    associateQueueSessionToBus(sessionB, nodeB.getBus());
    // Prevents broadcasting of messages so that they can be intercepted
    associateQueueSessionToBus(broadCastBlockingSession, nodeA.getBus());
    associateQueueSessionToBus(broadCastBlockingSession, nodeB.getBus());

    final Set<String> resultsSet = new HashSet<>();
    final String localService = "localTest";
    final CountDownLatch latch = new CountDownLatch(2);

    remoteSubscibeToTopic(sessionA, nodeA.getBus(), localService);
    remoteSubscibeToTopic(sessionB, nodeB.getBus(), localService);

    mockTransportWithAction(nodeA.getBus(), sessionA, msg -> {
      if (localService.equals(msg.getSubject())) {
        final String val = "Client 1:" + msg.get(String.class, MessageParts.Value);
        if (resultsSet.add(val)) {
          latch.countDown();
        }
        else {
          fail("Received duplicate message to Client 1.");
        }
      }
    });

    mockTransportWithAction(nodeB.getBus(), sessionB, msg -> {
      if (localService.equals(msg.getSubject())) {
        final String val = "Client 2:" + msg.get(String.class, MessageParts.Value);
        if (resultsSet.add(val)) {
          latch.countDown();
        }
        else {
          fail("Received duplicate message to Client 2.");
        }
      }
    });

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerA")
        .with(MessageParts.SessionID, sessionB.getSessionId())
        .noErrorHandling()
        .sendNowWith(nodeA.getBus());

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerB")
        .with(MessageParts.SessionID, sessionA.getSessionId())
        .noErrorHandling()
        .sendNowWith(nodeB.getBus());

    assertTrue("Timeout while waiting for messages from servers. Received: " + resultsSet, latch.await(30, TimeUnit.SECONDS));
    assertEquals(new HashSet<>(Arrays.asList("Client 1:ServerB", "Client 2:ServerA")), resultsSet);
  }

  public void testPointToPointMessageInClusterAfterClientChangesNodes() throws Exception {
    final ErraiService<?> nodeA = startInstance();
    final ErraiService<?> nodeB = startInstance();
    final ErraiService<?> nodeC = startInstance();
    final QueueSession session = MockQueueSessionFactory.newSession("client1");
    final QueueSession controlSession = MockQueueSessionFactory.newSession("control");
    final QueueSession broadCastBlockingSession = MockQueueSessionFactory.newSession("dummy");

    associateQueueSessionToBus(session, nodeA.getBus());
    // Listens for sent topic to control that it is not being broadcast
    associateQueueSessionToBus(controlSession, nodeA.getBus());
    associateQueueSessionToBus(controlSession, nodeB.getBus());
    associateQueueSessionToBus(controlSession, nodeC.getBus());
    // Prevents broadcasting of messages so that they can be intercepted
    associateQueueSessionToBus(broadCastBlockingSession, nodeA.getBus());
    associateQueueSessionToBus(broadCastBlockingSession, nodeB.getBus());
    associateQueueSessionToBus(broadCastBlockingSession, nodeC.getBus());

    final List<String> results = new ArrayList<>();
    final String localService = "localTest";
    final CountDownLatch latch1 = new CountDownLatch(1);

    remoteSubscibeToTopic(session, nodeA.getBus(), localService);

    mockTransportWithAction(nodeA.getBus(), session, msg -> {
      if (localService.equals(msg.getSubject())) {
        final String val = "Client:" + msg.get(String.class, MessageParts.Value);
        results.add(val);
        latch1.countDown();
      }
    });

    final Consumer<Message> failAction = msg -> {
      if (localService.equals(msg.getSubject())) {
        fail("Received message to wrong client session.");
      }
    };
    mockTransportWithAction(nodeA.getBus(), controlSession, failAction);
    mockTransportWithAction(nodeB.getBus(), controlSession, failAction);
    mockTransportWithAction(nodeC.getBus(), controlSession, failAction);

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerA")
        .with(MessageParts.SessionID, session.getSessionId())
        .noErrorHandling()
        .sendNowWith(nodeC.getBus());

    assertTrue("Timeout while waiting for first message from servers. Received: " + results, latch1.await(30, TimeUnit.SECONDS));
    assertEquals(Arrays.asList("Client:ServerA"), results);

    final CountDownLatch latch2 = new CountDownLatch(1);
    // Change bus and resubscribe to topic with new bus
    associateToNewBus(session, nodeA.getBus(), nodeB.getBus());
    remoteSubscibeToTopic(session, nodeB.getBus(), localService);
    mockTransportWithAction(nodeB.getBus(), session, msg -> {
      if (localService.equals(msg.getSubject())) {
        final String val = "Client:" + msg.get(String.class, MessageParts.Value);
        results.add(val);
        latch2.countDown();
      }
    });

    MessageBuilder.createMessage()
        .toSubject(localService)
        .signalling()
        .withValue("ServerB")
        .with(MessageParts.SessionID, session.getSessionId())
        .noErrorHandling()
        .sendNowWith(nodeC.getBus());

    assertTrue("Timeout while waiting for messages from servers. Received: " + results, latch2.await(30, TimeUnit.SECONDS));
    assertEquals(Arrays.asList("Client:ServerA", "Client:ServerB"), results);
  }

  /*
   * When a client switches servers in a cluster, it must associate with the new bus. This tests that
   * a message from an unassociated client is rejected with a QueueUnavailableException. Because
   * all Errai servlets catch this exception and send a disconnect, this behaviour ensures that
   * clients re-associate when a load-balancer switches them to a new server.
   */
  public void testBusThrowsQueueUnavailableExceptionForMessageFromClientThatHasNotAssociated() throws Exception {
    final ErraiService<?> node = startInstance();
    final QueueSession session = MockQueueSessionFactory.newSession("client1");

    final List<Message> received = new ArrayList<>();
    final String service = "service";
    node.getBus().subscribe(service, msg -> received.add(msg));

    final Message msg = MessageBuilder
            .createMessage(service)
            .signalling()
            .noErrorHandling()
            .getMessage()
            .setFlag(RoutingFlag.FromRemote)
            .setResource("Session", session)
            .setResource("SessionID", session);

    try {
      node.getBus().sendGlobal(msg);
      fail("No exception was thrown after message sent from unassociated bus!");
    }
    catch (final QueueUnavailableException ex) {
      // success
    }
    catch (final AssertionError ae) {
      throw ae;
    }
    catch (final Throwable t) {
      throw new AssertionError("Unexpected error after sending message from unassociated client.", t);
    }

    assertTrue(received.isEmpty());
    associateQueueSessionToBus(session, node.getBus());
    try {
      node.getBus().sendGlobal(msg);
      assertEquals("received = " + received, 1, received.size());
      assertSame(msg, received.get(0));
    }
    catch (final QueueUnavailableException ex) {
      fail("QueueUnavailableException thrown after associating.");
    }
    catch (final AssertionError ae) {
      throw ae;
    }
    catch (final Throwable t) {
      throw new AssertionError("Unexpected error after sending message from associated client.", t);
    }
  }

  private void associateToNewBus(final QueueSession session, final ServerMessageBus oldBus, final ServerMessageBus newBus) {
    final Message disconnectMsg = MessageBuilder
      .createMessage(BuiltInServices.ServerBus.name())
      .command(BusCommand.Disconnect)
      .noErrorHandling()
      .getMessage()
      .setResource("Session", session)
      .setResource("SessionID", session.getSessionId())
      .setFlag(RoutingFlag.FromRemote);

    oldBus.sendGlobal(disconnectMsg);
    associateQueueSessionToBus(session, newBus);
  }

  private void remoteSubscibeToTopic(final QueueSession session, final ServerMessageBus bus, final String subject) {
    final Message msg = MessageBuilder
      .createMessage(BuiltInServices.ServerBus.name())
      .command(BusCommand.RemoteSubscribe)
      .with(MessageParts.Subject, subject)
      .noErrorHandling()
      .getMessage()
      .setResource("Session", session)
      .setResource("SessionID", session.getSessionId())
      .setFlag(RoutingFlag.FromRemote);

    bus.sendGlobal(msg);
  }

  private void associateQueueSessionToBus(final QueueSession qs, final ServerMessageBus bus) {
    final Message msg = MessageBuilder
      .createMessage(BuiltInServices.ServerBus.name())
      .command(BusCommand.Associate)
      .with(MessageParts.RemoteServices, "ClientBus")
      .with(MessageParts.PriorityProcessing, 1)
      .noErrorHandling()
      .getMessage()
      .setResource("Session", qs)
      .setResource("SessionID", qs.getSessionId())
      .setFlag(RoutingFlag.FromRemote);

    bus.sendGlobal(msg);
  }

  private void mockTransportWithAction(final ServerMessageBus bus, final QueueSession qs, final Consumer<Message> action) {
    bus.getQueue(qs).setDeliveryHandler(new MessageDeliveryHandler() {

      @Override
      public void noop(final MessageQueue queue) throws IOException {}

      @Override
      public boolean deliver(final MessageQueue queue, final Message message) throws IOException {
        action.accept(message);
        return true;
      }
    });
  }
}
