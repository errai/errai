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
import org.jboss.errai.bus.client.api.PreInitializationListener;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.common.client.protocols.MessageParts;
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
  public void onModuleLoad() {
    final ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();

    final Runnable busReadyEvent = new Runnable() {
      public void run() {
        MessageBuilder.createMessage().toSubject("cdi.event:Dispatcher").command(CDICommands.AttachRemote).done()
                .sendNowWith(bus);

        CDI.fireEvent(new BusReadyEvent());
      }
    };

    bus.addPostInitTask(busReadyEvent);

    bus.subscribe("cdi.event:ClientDispatcher", new MessageCallback() {
      public void callback(Message message) {
        switch (BusCommands.valueOf(message.getCommandType())) {
          case RemoteSubscribe:
            CDI.addRemoteEventTypes(message.get(String[].class, MessageParts.Value));

            bus.addPostInitTask(new Runnable() {
              @Override
              public void run() {
                CDI.activate();
              }
            });
            break;
        }
      }
    });

    /*
     * Register an initialization lister to run the bus ready event.  This will be added
     * post-initialization, so it is designed to fire on bus reconnection events.
     */
    bus.addPostInitTask(new Runnable() {
      public void run() {
        bus.addPreInitializationListener(new PreInitializationListener() {
          public void beforeInitialization() {
            bus.addPostInitTask(busReadyEvent);
          }
        });
      }
    });
  }
}
