package org.jboss.errai.bus.server.cluster.jms;

import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.MessageHandler;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.cluster.ClusterCommands;
import org.jboss.errai.bus.server.cluster.ClusterParts;
import org.jboss.errai.bus.server.cluster.ClusteringProvider;
import org.jboss.errai.bus.server.cluster.IntrabusQueueSession;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.server.ServerMarshalling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class HornetQClusteringProvider implements ClusteringProvider {
  private static final String CLUSTER_SERVICE = "ErraiClusterService";
  private static final String BACKPLANE_SERVICE = "ErraiBackplane";
  private static final String ENVELOPE_PROPERTY = "JREP.envelope";

  private final ServerMessageBus serverMessageBus;

  private ClientProducer backplaneProducer;
  private ClientConsumer backplaneConsumer;
  private ClientSession clientSession;
  private final String busId = SecureHashUtil.nextSecureHash();
  private final int clusterPort;

  private Logger log = LoggerFactory.getLogger(HornetQClusteringProvider.class);

  private HornetQClusteringProvider(final ServerMessageBus bus, final int clusterPort) {
    this.serverMessageBus = bus;
    this.clusterPort = clusterPort;
  }

  public static ClusteringProvider create(final ServerMessageBus bus, final int clusterPort) throws Exception {
    final HornetQClusteringProvider qClusteringProvider = new HornetQClusteringProvider(bus, clusterPort);
    qClusteringProvider.setup();

    return qClusteringProvider;
  }

  private void setup() throws Exception {
    final Configuration configuration = new ConfigurationImpl();
    configuration.setPersistenceEnabled(false);
    configuration.setSecurityEnabled(false);
    configuration.setClustered(true);

    final Map<String, Object> transportParams = new HashMap<String, Object>();
    //transportParams.put(TransportConstants.HOST_PROP_NAME, "localhost");
    transportParams.put(TransportConstants.PORT_PROP_NAME, clusterPort);

    configuration.getAcceptorConfigurations()
        .add(new TransportConfiguration(NettyAcceptorFactory.class.getName(), transportParams));

    final HornetQServer server = HornetQServers.newHornetQServer(configuration);
    server.start();

    final ServerLocator serverLocator = HornetQClient
        .createServerLocatorWithoutHA(new TransportConfiguration(NettyConnectorFactory.class.getName(), transportParams));

    final ClientSessionFactory sf = serverLocator.createSessionFactory();

    final ClientSession coreSession = sf.createSession(false, false, false);

    final ClientSession.QueueQuery queueQuery = coreSession.queueQuery(SimpleString.toSimpleString(BACKPLANE_SERVICE));

    if (!queueQuery.isExists()) {
      coreSession.createQueue(BACKPLANE_SERVICE, BACKPLANE_SERVICE, false);
    }

    coreSession.close();

    clientSession = sf.createSession();
    clientSession.createQueue(busId, busId, false);

    final MessageHandler handler = new MessageHandler() {
      @Override
      public void onMessage(final ClientMessage message) {
        final String json = message.getStringProperty(ENVELOPE_PROPERTY);
        final Message commandMessage = MessageFactory.createCommandMessage(IntrabusQueueSession.INSTANCE, json);
        serverMessageBus.send(commandMessage);
      }
    };

    backplaneConsumer = clientSession.createConsumer(BACKPLANE_SERVICE);
    backplaneConsumer.setMessageHandler(handler);

    final ClientConsumer directConsumer = clientSession.createConsumer(busId);
    directConsumer.setMessageHandler(handler);

    backplaneProducer = clientSession.createProducer(BACKPLANE_SERVICE);

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
            if (serverMessageBus.hasRemoteSubscriptions(message.get(String.class, ClusterParts.Subject))) {
              final ClientMessage clientMessage = clientSession.createMessage(false);
              final String sessionIdRequested = message.get(String.class, MessageParts.SessionID);

              if (serverMessageBus.getQueueBySession(sessionIdRequested) == null) {
                return;
              }

              final Message replyMsg = CommandMessage.createWithParts(new HashMap<String, Object>())
                  .set(MessageParts.ToSubject, CLUSTER_SERVICE)
                  .set(MessageParts.CommandType, ClusterCommands.NotifyOwner.name())
                  .set(ClusterParts.BusId, busId)
                  .copy(ClusterParts.MessageId, message)
                  .set(MessageParts.SessionID, sessionIdRequested);

              clientMessage.putStringProperty(ENVELOPE_PROPERTY, ServerMarshalling.toJSON(replyMsg.getParts()));

              try {
                final ClientProducer producer = clientSession.createProducer(message.get(String.class, ClusterParts.BusId));
                producer.send(clientMessage);
                producer.close();
              }
              catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
          break;

          case NotifyOwner: {
            final String messageId = message.get(String.class, ClusterParts.MessageId);
            final Message deferredMessage = serverMessageBus.getSuspendedMessage(messageId);
            final String remoteBusId = message.get(String.class, ClusterParts.BusId);

            if (deferredMessage != null) {
              final Message dMessage = CommandMessage.createWithParts(new HashMap<String, Object>())
                  .set(MessageParts.ToSubject, CLUSTER_SERVICE)
                  .set(MessageParts.CommandType, ClusterCommands.MessageForward.name())
                  .set(ClusterParts.Payload, ServerMarshalling.toJSON(deferredMessage.getParts()));

              try {
                final ClientMessage clientMessage = clientSession.createMessage(false);
                final ClientProducer producer = clientSession.createProducer(dMessage.get(String.class, ClusterParts.BusId));
                producer.send(remoteBusId, clientMessage);
                producer.close();
              }
              catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
          break;

          case MessageForward: {
            final String payload = message.get(String.class, ClusterParts.Payload);
            final Message forwardMessage = MessageFactory.createCommandMessage(IntrabusQueueSession.INSTANCE, payload);
            serverMessageBus.send(forwardMessage);
          }
          break;
        }
      }
    };

    serverMessageBus.subscribe(CLUSTER_SERVICE, callback);

    clientSession.start();

    log.info("clustering subsystem started.");
  }

  @Override
  public void clusterTransmit(final String sessionId, final String subject, final String messageId) {
    final Message whoHandlesMessage = CommandMessage.createWithParts(new HashMap<String, Object>())
        .set(MessageParts.ToSubject, CLUSTER_SERVICE)
        .set(MessageParts.CommandType, ClusterCommands.WhoHandles.name())
        .set(MessageParts.SessionID, sessionId)
        .set(ClusterParts.BusId, busId)
        .set(ClusterParts.Subject, subject)
        .set(ClusterParts.MessageId, messageId);

    final ClientMessage clientMessage = clientSession.createMessage(false);
    clientMessage.putStringProperty(ENVELOPE_PROPERTY, ServerMarshalling.toJSON(whoHandlesMessage.getParts()));

    try {
      backplaneProducer.send(BACKPLANE_SERVICE, clientMessage);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

