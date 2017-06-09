/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.enterprise.client.cdi;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.enterprise.client.cdi.events.BusReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/**
 * The GWT entry point for the Errai CDI module.
 *
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class CDIClientBootstrap implements EntryPoint {
  private static final Logger logger = LoggerFactory.getLogger(CDIClientBootstrap.class);

  /*
   * These runnables must stay static so that they are not added multiple times to
   * InitVotes in tests.
   */

  final static Runnable declareServices = new Runnable() {
    @Override
    public void run() {
      final ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();
      if (!bus.isSubscribed(CDI.CLIENT_DISPATCHER_SUBJECT)) {
        logger.info("declare CDI dispatch service");
        bus.subscribe(CDI.CLIENT_DISPATCHER_SUBJECT, new MessageCallback() {
          @Override
          public void callback(final Message message) {
            switch (CDICommands.valueOf(message.getCommandType())) {
            case AttachRemote:
                CDI.activate(message.get(String.class, MessageParts.RemoteServices).split(","));
                break;

              case RemoteSubscribe:
                CDI.addRemoteEventTypes(message.get(String[].class, MessageParts.Value));
                break;

              case CDIEvent:
                CDI.consumeEventFromMessage(message);
                break;
              }
            }
        });
      }
    }
  };

  final static Runnable busInitRunnable = new Runnable() {
        private boolean firstRun = true;
        @Override
        public void run() {
          // Ensure that CDI system works after bus reconnection
          if (firstRun) {
            firstRun = false;
          }
          else {
            syncWithServer();
          }
          CDI.fireEvent(new BusReadyEvent());
        }

        @Override
        public String toString() {
          return "BusReadyEvent";
        }
      };

  private static void syncWithServer() {
    logger.info("CDI subsystem syncing with server ...");

    final BusErrorCallback serverDispatchErrorCallback = new BusErrorCallback() {
      @Override
      public boolean error(final Message message, final Throwable throwable) {
        try {
          throw throwable;
        }
        catch (final NoSubscribersToDeliverTo e) {
          logger.warn("Server did not subscribe to " + CDI.SERVER_DISPATCHER_SUBJECT +
              ". To activate the full Errai CDI functionality, make sure that Errai's Weld " +
              "integration module has been deployed on the server.");
          CDI.activate();
          return false;
        }
        catch (final Throwable t) {
          return true;
        }
      }
    };

    MessageBuilder.createMessage().toSubject(CDI.SERVER_DISPATCHER_SUBJECT)
        .command(CDICommands.AttachRemote)
        .errorsHandledBy(serverDispatchErrorCallback)
        .sendNowWith(ErraiBus.get());

    CDI.resendSubscriptionRequestForAllEventTypes();
  }

  @Override
  public void onModuleLoad() {
    logger.debug("Starting CDI module...");
    if (!EventQualifierSerializer.isSet()) {
      EventQualifierSerializer.set(GWT.create(EventQualifierSerializer.class));
    }
    InitVotes.registerPersistentPreInitCallback(declareServices);
    InitVotes.waitFor(CDI.class);

    if (BusToolsCli.isRemoteCommunicationEnabled()) {
      syncWithServer();
      InitVotes.registerPersistentDependencyCallback(ClientMessageBus.class, busInitRunnable);
    }
    else {
      CDI.activate();
      CDI.fireEvent(new BusReadyEvent());
    }
  }
}
