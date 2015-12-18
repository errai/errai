/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.cluster.jgroups;

import static org.jboss.errai.bus.server.cluster.ClusterParts.BusId;
import static org.jboss.errai.bus.server.cluster.ClusterParts.MessageId;
import static org.jboss.errai.bus.server.cluster.ClusterParts.Payload;
import static org.jboss.errai.bus.server.cluster.ClusterParts.SessId;
import static org.jboss.errai.bus.server.cluster.ClusterParts.Subject;
import static org.jboss.errai.common.client.protocols.MessageParts.CommandType;
import static org.jboss.errai.common.client.protocols.MessageParts.SessionID;
import static org.jboss.errai.common.client.protocols.MessageParts.ToSubject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.QueueUnavailableException;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.cluster.ClusterCommands;
import org.jboss.errai.bus.server.cluster.ClusterParts;
import org.jboss.errai.bus.server.cluster.ClusteringProvider;
import org.jboss.errai.bus.server.cluster.IntrabusQueueSession;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.common.client.protocols.Resources;
import org.jboss.errai.marshalling.client.protocols.ErraiProtocol;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mike Brock
 */
public class JGroupsClusteringProvider extends ReceiverAdapter implements ClusteringProvider, MessageCallback {

  private static final String CLUSTER_SERVICE = "local:ErraiClusterService"; // erraibus service
  private final String busId = SecureHashUtil.nextSecureHash();

  private final JChannel jchannel;
  private final ServerMessageBus serverMessageBus;

  final Cache<String, Address> sessionToNodeCache;

  private final static String JGROUPS_MESSAGE_RESOURCE = "JGroupsMessage";

  private static Logger log = LoggerFactory.getLogger(JGroupsClusteringProvider.class);

  @Inject
  private JGroupsClusteringProvider(final ServerMessageBus messageBus,
                                    final ErraiServiceConfigurator config,
                                    final ErraiService erraiService) {
    this.serverMessageBus = messageBus;

    try {
      jchannel = new JChannel(JGroupsConfigAttribs.JGROUPS_PROTOCOL_STACK.get(config));
      jchannel.connect(ErraiConfigAttribs.CLUSTER_NAME.get(config));

      // I don't think waiting for the state is necessary.
      // jchannel.getState(null, 2000);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    serverMessageBus.subscribe(CLUSTER_SERVICE, this);
    jchannel.setReceiver(this);

    erraiService.addShutdownHook(new Runnable() {
      @Override
      public void run() {
        jchannel.close();
        log.info("shut down jgroups clustering service");
      }
    });

    sessionToNodeCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .build();

    log.info("starting errai clustering service.");
  }

  @Override
  public void receive(final org.jgroups.Message msg) {
    try {
      final Message erraiMessage = getErraiMessage(msg);
      erraiMessage.setResource(JGROUPS_MESSAGE_RESOURCE, msg);

      if (busId.equals(erraiMessage.get(String.class, BusId))) {
        return;
      }
      erraiMessage.setFlag(RoutingFlag.FromPeer);

      serverMessageBus.sendGlobal(erraiMessage);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void callback(final Message message) {
    final QueueSession queueSession = message.getResource(QueueSession.class, "Session");
    if (queueSession != IntrabusQueueSession.INSTANCE) {
      log.warn("message to cluster service ('" + CLUSTER_SERVICE + "') originating from illegal session. " +
          " message was discarded.");
      return;
    }

    switch (ClusterCommands.valueOf(message.getCommandType())) {
      case WhoHandles: {
        final String subject = message.get(String.class, Subject);
        if (serverMessageBus.hasRemoteSubscriptions(subject)) {
          final String sessionIdRequested = message.get(String.class, ClusterParts.SessId);

          try {
            if (serverMessageBus.getQueueBySession(sessionIdRequested) == null) {
              return;
            }
          }
          catch (QueueUnavailableException e) {
            return;
          }

          final org.jgroups.Message jgroupsMessage = message.getResource(org.jgroups.Message.class, JGROUPS_MESSAGE_RESOURCE);

          final Message replyMsg = CommandMessage.create()
              .set(ToSubject, CLUSTER_SERVICE)
              .set(CommandType, ClusterCommands.NotifyOwner.name())
              .set(BusId, busId)
              .copy(MessageId, message)
              .set(ClusterParts.SessId, sessionIdRequested);

          try {
            jchannel.send(jgroupsMessage.getSrc(), ErraiProtocol.encodePayload(replyMsg.getParts()));
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      break;

      case NotifyOwner: {
        final String messageId = message.get(String.class, MessageId);
        final String sessId = message.get(String.class, SessId);
        final Message deferredMessage = serverMessageBus.getDeadLetterMessage(messageId);
        serverMessageBus.removeDeadLetterMessage(messageId);

        final org.jgroups.Message jgroupsMessage
            = message.getResource(org.jgroups.Message.class, JGROUPS_MESSAGE_RESOURCE);

        sessionToNodeCache.put(sessId, jgroupsMessage.getSrc());

        if (deferredMessage != null) {
          final Message dMessage = createForwardMessageFor(deferredMessage, messageId);

          try {
            jchannel.send(jgroupsMessage.getSrc(), ErraiProtocol.encodePayload(dMessage.getParts()));
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      break;

      case InvalidRoute: {
        final String sessionId = message.get(String.class, SessId);
        sessionToNodeCache.invalidate(sessionId);

        final String messageId = message.get(String.class, MessageId);
        final String subject = message.get(String.class, Subject);

        final Message whoMessage = createWhoHandlesMessage(sessionId, subject, messageId);

        try {
          jchannel.send(getJGroupsMessage(whoMessage));
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        break;
      }

      case MessageForward: {
        final String payload = message.get(String.class, Payload);
        final Message forwardMessage = MessageFactory.createCommandMessage(IntrabusQueueSession.INSTANCE, payload);
        forwardMessage.setFlag(RoutingFlag.FromPeer);

        final String sessId = message.get(String.class, SessId);
        if (sessId == null) {
          serverMessageBus.sendGlobal(forwardMessage);
        }
        else {
          final MessageQueue messageQueue;

          try {
            messageQueue = serverMessageBus.getQueueBySession(sessId);
          }
          catch (QueueUnavailableException e) {
            final org.jgroups.Message jgroupsMessage
                = message.getResource(org.jgroups.Message.class, JGROUPS_MESSAGE_RESOURCE);

            final String messageId = message.get(String.class, MessageId);
            final Message invalidRoute = createInvalidRouteMessage(sessId, forwardMessage.getSubject(), messageId);

            try {
              jchannel.send(jgroupsMessage.getSrc(), ErraiProtocol.encodePayload(invalidRoute.getParts()));
            }
            catch (Exception e2) {
              e2.printStackTrace();
            }
            return;
          }

          // otherwise route it directly to the client.
          forwardMessage.setResource(Resources.Session.name(), messageQueue.getSession());
          serverMessageBus.send(forwardMessage);
        }
      }
      break;
    }
  }

  @Override
  public void clusterTransmit(final String sessionId, final String subject, final String messageId) {
    final Address knownAddress = sessionToNodeCache.getIfPresent(sessionId);
    if (knownAddress != null) {
      final Message forwardMessage = createForwardMessageFor(serverMessageBus.getDeadLetterMessage(messageId), messageId);
      try {
        jchannel.send(knownAddress, ErraiProtocol.encodePayload(forwardMessage.getParts()));
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    else {
      final Message whoHandlesMessage = createWhoHandlesMessage(sessionId, subject, messageId);
      try {
        jchannel.send(getJGroupsMessage(whoHandlesMessage));
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private Message createForwardMessageFor(final Message message, final String messageId) {
    final Message forward = CommandMessage.create()
        .set(ToSubject, CLUSTER_SERVICE)
        .set(CommandType, ClusterCommands.MessageForward.name())
        .set(Payload, ErraiProtocol.encodePayload(message.getParts()))
        .set(BusId, busId);

    if (message.hasPart(SessionID)) {
      final String value = message.get(String.class, SessionID);
      if (!IntrabusQueueSession.INSTANCE.getSessionId().equals(value)) {
        forward.set(SessId, value);
      }
    }

    if (messageId != null) {
      forward.set(MessageId, messageId);
    }

    return forward;
  }

  private Message createInvalidRouteMessage(final String sessionId, final String subject, final String messageId) {
    return CommandMessage.create()
        .set(ToSubject, CLUSTER_SERVICE)
        .set(CommandType, ClusterCommands.InvalidRoute.name())
        .set(SessId, sessionId)
        .set(Subject, subject)
        .set(MessageId, messageId)
        .set(BusId, busId);
  }

  private Message createWhoHandlesMessage(final String sessionId, final String subject, final String messageId) {
    return CommandMessage.create()
        .set(ToSubject, CLUSTER_SERVICE)
        .set(CommandType, ClusterCommands.WhoHandles.name())
        .set(ClusterParts.SessId, sessionId)
        .set(BusId, busId)
        .set(Subject, subject)
        .set(MessageId, messageId);
  }

  @Override
  public void clusterTransmitGlobal(final Message message) {
    try {
      jchannel.send(getJGroupsMessage(createForwardMessageFor(message, null)));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Message getErraiMessage(final org.jgroups.Message message) {
    return MessageFactory.createCommandMessage(IntrabusQueueSession.INSTANCE, String.valueOf(message.getObject()));
  }

  private static org.jgroups.Message getJGroupsMessage(final Message message) {
    return new org.jgroups.Message(null, null, ErraiProtocol.encodePayload(message.getParts()));
  }
}
