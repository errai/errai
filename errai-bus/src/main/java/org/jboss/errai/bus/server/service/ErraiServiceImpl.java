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

package org.jboss.errai.bus.server.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.QueueSession;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.*;
import org.jboss.errai.bus.server.security.auth.AuthSubject;
import org.jboss.errai.bus.server.security.auth.AuthenticationAdapter;
import org.jboss.errai.bus.server.security.auth.AuthenticationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;

import static com.google.inject.Guice.createInjector;

/**
 * Default implementation of the ErraiBus server-side service.
 */
@Singleton
public class ErraiServiceImpl implements ErraiService {
    private ServerMessageBus bus;
    private RequestDispatcher dispatcher;
    private ErraiServiceConfigurator configurator;

    private Logger log = LoggerFactory.getLogger("ErraiBootstrap");

    @Inject
    public ErraiServiceImpl(final ServerMessageBus bus,
                            final ErraiServiceConfigurator configurator) {
        this.bus = bus;
        this.configurator = configurator;

        init();
    }

    private void init() {
        //todo: this all needs sendNowWith be refactored at some point.
        bus.subscribe(AUTHORIZATION_SVC_SUBJECT, new MessageCallback() {
            public void callback(Message c) {
                switch (SecurityCommands.valueOf(c.getCommandType())) {
                    case WhatCredentials:
                        /**
                         * Respond with what credentials the authentication system requires.
                         */
                        //todo: we only support login/password for now

                        ConversationMessage.create(c)
                                .command(SecurityCommands.WhatCredentials)
                                .set(SecurityParts.CredentialsRequired, "Name,Password")
                                .set(MessageParts.ReplyTo, AUTHORIZATION_SVC_SUBJECT)
                                .sendNowWith(bus);

                        break;

                    case AuthRequest:
                        /**
                         * Send a challenge.
                         */

                        try {
                            configurator.getResource(AuthenticationAdapter.class)
                                    .challenge(c);
                        }
                        catch (AuthenticationFailedException a) {
                        }
                        break;

                    case EndSession:
                        configurator.getResource(AuthenticationAdapter.class)
                                .endSession(c);

                        bus.send(ConversationMessage.create(c).toSubject("LoginClient")
                                .command(SecurityCommands.EndSession));
                        break;
                }
            }
        });


        /**
         * The standard ServerEchoService.
         */
        bus.subscribe("ServerEchoService", new MessageCallback() {
            public void callback(Message c) {
                bus.send(ConversationMessage.create(c));
            }
        });

        bus.subscribe("ClientNegotiationService", new MessageCallback() {
            public void callback(Message message) {
                AuthSubject subject =  message.getResource(QueueSession.class, "Session")
                        .getAttribute(AuthSubject.class, ErraiService.SESSION_AUTH_DATA);

                ConversationMessage reply = ConversationMessage.create(message);

                if (subject != null) {
                    reply.set(SecurityParts.Roles, subject.toRolesString());
                    reply.set(SecurityParts.Name, subject.getUsername());
                }

                reply.sendNowWith(bus);
            }
        });

        configurator.configure(this);
        dispatcher = configurator.getConfiguredDispatcher();
        bus.configure(configurator);
        final ErraiService erraiSvc = this;
    }

    public void store(Message message) {

        message.addResources(configurator.getResourceProviders());

        /**
         * Pass the message off to the messaging bus for handling.
         */
        try {
            dispatcher.dispatchGlobal(message);
            // bus.sendGlobal(message);
        }
        catch (Throwable t) {
            System.err.println("Message was not delivered.");
            t.printStackTrace();
        }
    }


//    public void storeAsync(final CommandMessage message) {
//        message.addResources(configurator.getResourceProviders());
//        bus.sendGlobalAsync(message);
//    }

    public ServerMessageBus getBus() {
        return bus;
    }

    public ErraiServiceConfigurator getConfiguration() {
        return configurator;
    }
}
