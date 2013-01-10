/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import static org.jboss.errai.bus.server.cluster.ClusterParts.Subject;
import static org.jboss.errai.common.client.protocols.MessageParts.CommandType;
import static org.jboss.errai.common.client.protocols.MessageParts.SessionID;
import static org.jboss.errai.common.client.protocols.MessageParts.ToSubject;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.RoutingFlag;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.cluster.ClusterCommands;
import org.jboss.errai.bus.server.cluster.ClusteringProvider;
import org.jboss.errai.bus.server.cluster.IntrabusQueueSession;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.marshalling.client.protocols.ErraiProtocol;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * @author Mike Brock
 */
public class JGroupsClusteringProvider extends ReceiverAdapter implements ClusteringProvider, MessageCallback {

  private static final String CLUSTER_SERVICE = "local:ErraiClusterService"; // erraibus service
  private final String busId = SecureHashUtil.nextSecureHash();

  private final JChannel jchannel;
  private final ServerMessageBus serverMessageBus;

  private static Logger log = LoggerFactory.getLogger(JGroupsClusteringProvider.class);

  @Inject
  private JGroupsClusteringProvider(final ServerMessageBus messageBus, final ErraiServiceConfigurator config) {
    this.serverMessageBus = messageBus;

    try {
      jchannel = new JChannel();
      jchannel.connect(ErraiConfigAttribs.CLUSTER_NAME.get(config));

      // I don't think waiting for the state is necessary.
      // jchannel.getState(null, 2000);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    serverMessageBus.subscribe(CLUSTER_SERVICE, this);
    jchannel.setReceiver(this);

    log.info("starting errai clustering service.");
  }

  @Override
  public void receive(final org.jgroups.Message msg) {
    try {
      final Message erraiMessage = getErraiMessage(msg);

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
//            if (serverMessageBus.hasRemoteSubscriptions(message.get(String.class, Subject))) {
//              final ClientMessage clientMessage = clientSession.createMessage(false);
//              final String sessionIdRequested = message.get(String.class, SessionID);
//
//              if (serverMessageBus.getQueueBySession(sessionIdRequested) == null) {
//                return;
//              }
//
//              final Message replyMsg = CommandMessage.createWithParts(new HashMap<String, Object>())
//                  .set(ToSubject, CLUSTER_SERVICE)
//                  .set(CommandType, ClusterCommands.NotifyOwner.name())
//                  .set(BusId, busId)
//                  .copy(MessageId, message)
//                  .set(SessionID, sessionIdRequested);
//
//              clientMessage.putStringProperty(ENVELOPE_PROPERTY, ErraiProtocol.encodePayload(replyMsg.getParts()));
//
//              try {
//                final ClientProducer producer = clientSession.createProducer(message.get(String.class, BusId));
//                producer.send(clientMessage);
//                producer.close();
//              }
//              catch (Exception e) {
//                e.printStackTrace();
//              }
//            }
      }
      break;

      case NotifyOwner: {
        final String messageId = message.get(String.class, MessageId);
        final Message deferredMessage = serverMessageBus.getSuspendedMessage(messageId);
        final String remoteBusId = message.get(String.class, BusId);

        if (deferredMessage != null) {
          final Message dMessage = CommandMessage.createWithParts(new HashMap<String, Object>())
              .set(ToSubject, CLUSTER_SERVICE)
              .set(CommandType, ClusterCommands.MessageForward.name())
              .set(Payload, ErraiProtocol.encodePayload(deferredMessage.getParts()));

          try {
//                final ClientMessage clientMessage = clientSession.createMessage(false);
//                final ClientProducer producer = clientSession.createProducer(dMessage.get(String.class, BusId));
//                producer.send(remoteBusId, clientMessage);
//                producer.close();
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      break;

      case MessageForward: {
        final String payload = message.get(String.class, Payload);
        final Message forwardMessage = MessageFactory.createCommandMessage(IntrabusQueueSession.INSTANCE, payload);
        forwardMessage.setFlag(RoutingFlag.FromPeer);
        serverMessageBus.sendGlobal(forwardMessage);
      }
      break;
    }
  }

  @Override
  public void clusterTransmit(final String sessionId, final String subject, final String messageId) {
    final Message whoHandlesMessage = CommandMessage.createWithParts(new HashMap<String, Object>())
        .set(ToSubject, CLUSTER_SERVICE)
        .set(CommandType, ClusterCommands.WhoHandles.name())
        .set(SessionID, sessionId)
        .set(BusId, busId)
        .set(Subject, subject)
        .set(MessageId, messageId);


    try {
      jchannel.send(getJGroupsMessage(whoHandlesMessage));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Override
  public void clusterTransmitGlobal(final Message message) {
    final Message dMessage = CommandMessage.createWithParts(new HashMap<String, Object>())
        .set(ToSubject, CLUSTER_SERVICE)
        .set(CommandType, ClusterCommands.MessageForward.name())
        .set(Payload, ErraiProtocol.encodePayload(message.getParts()))
        .set(BusId, busId);

    try {
      jchannel.send(getJGroupsMessage(dMessage));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Message getErraiMessage(final org.jgroups.Message message) {
    try {
      return MessageFactory.createCommandMessage(IntrabusQueueSession.INSTANCE, new String(message.getRawBuffer(), "UTF-8"));
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private static org.jgroups.Message getJGroupsMessage(final Message message) {
    try {
      return new org.jgroups.Message(null, null, ErraiProtocol.encodePayload(message.getParts()).getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
