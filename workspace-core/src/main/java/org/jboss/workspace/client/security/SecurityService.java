package org.jboss.workspace.client.security;

import org.jboss.workspace.client.framework.MessageCallback;
import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.MessageBusClient;
import org.jboss.workspace.client.rpc.protocols.SecurityCommands;
import org.jboss.workspace.client.rpc.protocols.SecurityParts;
import static org.jboss.workspace.client.rpc.protocols.SecurityParts.CredentialsRequired;
import org.jboss.workspace.client.security.impl.BasicAuthenticationContext;
import org.jboss.workspace.client.security.impl.NameCredential;
import org.jboss.workspace.client.security.impl.PasswordCredential;

import java.util.HashSet;
import java.util.Set;

public class SecurityService {
    private AuthenticationContext authenticationContext;
    
    public void doAuthentication(final String name, final AuthenticationHandler handler) {
        final String responseSubject = "org.jboss.workspace.authentication." + name;
        MessageBusClient.subscribe(responseSubject, new MessageCallback() {
            public void callback(CommandMessage msg) {
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
                        challenge.set(SecurityParts.ReplyTo, responseSubject);

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

                        MessageBusClient.store("AuthenticationService", challenge);
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

    }

}
