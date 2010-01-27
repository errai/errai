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

package org.jboss.errai.bus.client.security;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;

import static org.jboss.errai.bus.client.MessageBuilder.createMessage;
import static org.jboss.errai.bus.client.protocols.SecurityParts.CredentialsRequired;

import org.jboss.errai.bus.client.security.impl.BasicAuthenticationContext;
import org.jboss.errai.bus.client.security.impl.NameCredential;
import org.jboss.errai.bus.client.security.impl.PasswordCredential;

import java.util.HashSet;
import java.util.Set;

public class SecurityService {
    private AuthenticationContext authenticationContext;
    private AuthenticationHandler authHandler;
    public static final String SUBJECT = "ClientAuthenticationService";

    public SecurityService() {
        ErraiBus.get().subscribe(SUBJECT, new MessageCallback() {
            public void callback(Message msg) {
             
                switch (SecurityCommands.valueOf(msg.getCommandType())) {
                    case DemandCredentials:
                        if (authHandler == null) {
             //               msg.toSubject("LoginClient").sendNowWith(ErraiBus.get());
                            return;
                        }

                        String credentialsRequired = msg.get(String.class, CredentialsRequired);
                        String[] credentialNames = credentialsRequired.split(",");
                        Credential[] credentials = new Credential[credentialNames.length];

                        for (int i = 0; i < credentialNames.length; i++) {
                            switch (CredentialTypes.valueOf(credentialNames[i])) {
                                case Name:
                                    credentials[i] = new NameCredential();
                                    break;
                                case Password:
                                    credentials[i] = new PasswordCredential();
                                    break;

                                default:
                                    //todo: throw a massive error here.
                            }
                        }

                        authHandler.doLogin(credentials);

                        Message challenge = createMessage()
                                .toSubject("AuthenticationService")
                                .command(SecurityCommands.AuthRequest)
                                .with(MessageParts.ReplyTo, SUBJECT)
                                .getMessage();

                        for (int i = 0; i < credentialNames.length; i++) {
                            switch (CredentialTypes.valueOf(credentialNames[i])) {
                                case Name:
                                    challenge.set(CredentialTypes.Name, credentials[i].getValue());
                                    break;
                                case Password:
                                    challenge.set(CredentialTypes.Password, credentials[i].getValue());
                                    break;
                            }
                        }

                        challenge.sendNowWith(ErraiBus.get());

                        break;
                    case AuthenticationNotRequired:
                        msg.toSubject("LoginClient").sendNowWith(ErraiBus.get());
                        break;

                    case FailedAuth:
                    case SuccessfulAuth:
                        String name = msg.get(String.class, SecurityParts.Name);
                        String rolesString = msg.get(String.class, SecurityParts.Roles);

                        if (authenticationContext != null && authenticationContext.isValid()) return;

                        Set<Role> roleSet = new HashSet<Role>();
                        if (rolesString != null) {
                            String[] roles = rolesString.split(",");

                            for (final String role : roles) {
                                roleSet.add(new Role() {
                                    public String getRoleName() {
                                        return role;
                                    }
                                });
                            }
                        } else {

                        }
                        authenticationContext = new BasicAuthenticationContext(roleSet, name);

                        // forward this message on to the login client.
                        msg.toSubject("LoginClient").sendNowWith(ErraiBus.get());

                        break;
                }
            }
        });
    }

    public void doAuthentication(final AuthenticationHandler handler) {
        authHandler = handler;

        DeferredCommand.addCommand(new Command() {
            @Override
            public void execute() {
                MessageBuilder.createMessage()
                        .toSubject("AuthenticationService")
                        .command(SecurityCommands.DemandCredentials)
                        .with(MessageParts.ReplyTo, SUBJECT)
                        .noErrorHandling().sendNowWith(ErraiBus.get());
            }
        });
    }

    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }
}
