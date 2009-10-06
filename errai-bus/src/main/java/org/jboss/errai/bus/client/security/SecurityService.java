package org.jboss.errai.bus.client.security;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ErraiClient;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import static org.jboss.errai.bus.client.protocols.SecurityParts.CredentialsRequired;
import org.jboss.errai.bus.client.security.impl.BasicAuthenticationContext;
import org.jboss.errai.bus.client.security.impl.NameCredential;
import org.jboss.errai.bus.client.security.impl.PasswordCredential;

import java.util.HashSet;
import java.util.Set;

public class SecurityService {
    private AuthenticationContext authenticationContext;
    public static final String SUBJECT = "ClientAuthenticationService";

    public SecurityService() {
    }

    public void doAuthentication(final AuthenticationHandler handler) {
      
        ErraiClient.getBus().subscribe(SUBJECT, new MessageCallback() {
            public void callback(CommandMessage msg) {
                ErraiClient.getBus().unsubscribeAll(SUBJECT);

                switch (SecurityCommands.valueOf(msg.getCommandType())) {
                    case WhatCredentials:
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

                        handler.doLogin(credentials);

                        CommandMessage challenge = new CommandMessage(SecurityCommands.AuthRequest.name());
                        challenge.set(SecurityParts.ReplyTo, SUBJECT);

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

                        ErraiClient.getBus().send("AuthorizationService", challenge);

                        break;

                    case SecurityResponse:
                        String name = msg.get(String.class, SecurityParts.Name);
                        String rolesString =  msg.get(String.class, SecurityParts.Roles);

                        if (authenticationContext != null && authenticationContext.isValid()) return;

                        String[] roles =  rolesString.split(",");

                        Set<Role> roleSet = new HashSet<Role>();
                        for (final String role : roles) {
                            roleSet.add(new Role() {
                                public String getRoleName() {
                                    return role;
                                }
                            });
                        }

                        authenticationContext = new BasicAuthenticationContext(roleSet, name);

                        break;

                }
            }
        });


        CommandMessage message = new CommandMessage(SecurityCommands.WhatCredentials);
        message.set(SecurityParts.ReplyTo, SUBJECT);
        ErraiClient.getBus().send("AuthorizationService", message);
    }

}
