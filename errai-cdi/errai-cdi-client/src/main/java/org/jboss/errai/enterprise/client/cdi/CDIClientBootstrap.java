/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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
package org.jboss.errai.enterprise.client.cdi;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.LogUtil;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.enterprise.client.cdi.events.BusReadyEvent;

import com.google.gwt.core.client.EntryPoint;

/**
 * The GWT entry point for the Errai CDI module.
 *
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class CDIClientBootstrap implements EntryPoint {
  static final ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();

  final static Runnable initRemoteCdiSubsystem = new Runnable() {

    public void run() {
      LogUtil.log("CDI subsystem syncing with server ...");
      MessageBuilder.createMessage().toSubject(CDI.SERVER_DISPATCHER_SUBJECT)
          .command(CDICommands.AttachRemote)
          .done()
          .sendNowWith(bus);

      CDI.fireEvent(new BusReadyEvent());
    }

    public String toString() {
      return "BusReadyEvent";
    }
  };

  final static Runnable declareServices = new Runnable() {
    final ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();

    @Override
    public void run() {
      if (!bus.isSubscribed(CDI.CLIENT_DISPATCHER_SUBJECT)) {
        LogUtil.log("declare CDI dispatch service");
        bus.subscribe(CDI.CLIENT_DISPATCHER_SUBJECT, new MessageCallback() {
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

  public void onModuleLoad() {
    InitVotes.registerPersistentPreInitCallback(declareServices);
    InitVotes.waitFor(CDI.class);

    if (BusToolsCli.isRemoteCommunicationEnabled()) {
      InitVotes.registerPersistentDependencyCallback(ClientMessageBus.class, initRemoteCdiSubsystem);
    }
    else {
      CDI.activate();
      CDI.fireEvent(new BusReadyEvent());
    }
  }
}
