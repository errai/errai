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
    private AuthenticationAdapter authenticationAdapter;
    private ErraiServiceConfigurator configurator;

    @Inject
    public ErraiServiceImpl(ServerMessageBus bus, AuthenticationAdapter authenticationAdapter,
                            ErraiServiceConfigurator configurator) {
        this.bus = bus;
        this.authenticationAdapter = authenticationAdapter;
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
                        bus.send(c.get(String.class, SecurityParts.ReplyTo),
                                ConversationMessage.create(SecurityCommands.WhatCredentials, c)
                                        .set(SecurityParts.CredentialsRequired, "Name,Password")
                                        .set(SecurityParts.ReplyTo, AUTHORIZATION_SVC_SUBJECT));
                        break;

                    case AuthRequest:
                        /**
                         * Send a challenge.
                         */
                        authenticationAdapter.challenge(c);
                        break;

                    case EndSession:
                        authenticationAdapter.endSession(c);
                        bus.send(ConversationMessage.create(c).toSubject("LoginClient")
                                .setCommandType(SecurityCommands.SecurityChallenge));
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
