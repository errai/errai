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

import java.util.HashMap;

/**
 * @author Mike Brock
 */
public class JGroupsClusteringProvider implements ClusteringProvider {

  private static final String CLUSTER_SERVICE = "ErraiClusterService"; // erraibus service
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

    final MessageCallback callback = new MessageCallback() {
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
    };

    serverMessageBus.subscribe(CLUSTER_SERVICE, callback);

    jchannel.setReceiver(new ReceiverAdapter() {
      @Override
      public void receive(final org.jgroups.Message msg) {
        try {
          final String json = new String(msg.getRawBuffer(), "UTF-8");
          final Message commandMessage = MessageFactory.createCommandMessage(IntrabusQueueSession.INSTANCE, json);

          if (busId.equals(commandMessage.get(String.class, BusId))) {
            return;
          }
          commandMessage.setFlag(RoutingFlag.FromPeer);

          serverMessageBus.sendGlobal(commandMessage);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }


    });

    log.info("starting errai clustering service.");
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


    final org.jgroups.Message jGroupsMsg = new org.jgroups.Message(null, null, ErraiProtocol.encodePayload(whoHandlesMessage.getParts()));

    try {
      jchannel.send(jGroupsMsg);
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
      final org.jgroups.Message jGroupsMsg
          = new org.jgroups.Message(null, null, ErraiProtocol.encodePayload(dMessage.getParts()).getBytes("UTF-8"));
      jchannel.send(jGroupsMsg);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
