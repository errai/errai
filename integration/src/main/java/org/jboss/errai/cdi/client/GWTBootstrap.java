/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.client;

import com.google.gwt.core.client.EntryPoint;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.BusEvent;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.cdi.client.events.BusReadyEvent;

/**
 * The GWT entry point
 */
public class GWTBootstrap implements EntryPoint {
    public void onModuleLoad() {
        final ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();

        // conversation interceptor
        bus.addInterceptor(CDI.CONVERSATION_INTERCEPTOR);
        bus.addPostInitTask(new Runnable() {
            public void run() {
                MessageBuilder.createMessage()
                        .toSubject("cdi.event:Dispatcher")
                        .command(CDICommands.AttachRemote)
                        .done().sendNowWith(bus);

                CDI.fireEvent(new BusReadyEvent());
            }
        });

        bus.subscribe("cdi.event:ClientDispatcher", new MessageCallback() {
            public void callback(Message message) {
                switch (BusCommands.valueOf(message.getCommandType())) {
                    case RemoteSubscribe:
                        CDI.addRemoteEventTypes(message.get(String[].class, MessageParts.Value));
                        CDI.activate();
                        break;

                }
            }
        });
    }
}
