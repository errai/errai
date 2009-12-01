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
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.security.auth.AuthSubject;
import org.jboss.errai.bus.server.security.auth.AuthenticationAdapter;

import javax.servlet.http.HttpSession;

public class ErraiServiceImpl implements ErraiService {
    private ServerMessageBus bus;
    private ErraiServiceConfigurator configurator;

    @Inject
    public ErraiServiceImpl(ServerMessageBus bus,
                            ErraiServiceConfigurator configurator) {
        this.bus = bus;
        this.configurator = configurator;

        init();
    }

    private void init() {
        // just use the simple bus for now.  more integration options sendNowWith come...
        //bus.addGlobalListener(new BasicAuthorizationListener(authorizationAdapter, bus));

        //todo: this all needs sendNowWith be refactored at some point.
        bus.subscribe(AUTHORIZATION_SVC_SUBJECT, new MessageCallback() {
            public void callback(CommandMessage c) {
                switch (SecurityCommands.valueOf(c.getCommandType())) {
                    case WhatCredentials:
                        /**
                         * Respond with what credentials the authentication system requires.
                         */
                        //todo: we only support login/password for now


                        ConversationMessage.create(c)
                               .command(SecurityCommands.WhatCredentials)
                                .set(SecurityParts.CredentialsRequired, "Name,Password")
                                .set(SecurityParts.ReplyTo, AUTHORIZATION_SVC_SUBJECT)
                                .sendNowWith(bus);

                        break;

                    case AuthRequest:
                        /**
                         * Send a challenge.
                         */

                        configurator.getResource(AuthenticationAdapter.class)
                                .challenge(c);
                        break;

                    case EndSession:
                        configurator.getResource(AuthenticationAdapter.class)
                                .challenge(c);
                        bus.send(ConversationMessage.create(c).toSubject("LoginClient")
                                .command(SecurityCommands.SecurityChallenge));
                        break;
                }
            }
        });


        /**
         * The standard ServerEchoService.
         */
        bus.subscribe("ServerEchoService", new MessageCallback() {
            public void callback(CommandMessage c) {
                bus.send(ConversationMessage.create(c));
            }
        });

        bus.subscribe("ClientNegotiationService", new MessageCallback() {
            public void callback(CommandMessage message) {
                AuthSubject subject = (AuthSubject)
                        ((HttpSession) message.getResource("Session")).getAttribute(ErraiService.SESSION_AUTH_DATA);

                ConversationMessage reply = ConversationMessage.create(message);

                if (subject != null) {
                    reply.set(SecurityParts.Roles, subject.toRolesString());
                    reply.set(SecurityParts.Name, subject.getUsername());
                }

                reply.sendNowWith(bus);
            }
        });

        configurator.configure();
    }

    public void store(CommandMessage message) {

        message.addResources(configurator.getResourceProviders());

        /**
         * Pass the message off to the messaging bus for handling.
         */
        try {
            bus.sendGlobal(message);
        }
        catch (Throwable t) {
            System.err.println("Message was not delivered.");
            t.printStackTrace();
        }
    }



    public ServerMessageBus getBus() {
        return bus;
    }
}
