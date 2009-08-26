package org.jboss.workspace.client.security;

import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.rpc.MessageBusClient;
import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.protocols.SecurityCommands;
import org.jboss.workspace.client.rpc.protocols.SecurityParts;
import static org.jboss.workspace.client.rpc.protocols.SecurityParts.CommandType;
import static org.jboss.workspace.client.rpc.protocols.SecurityParts.CredentialsRequired;
import org.jboss.workspace.client.security.impl.NameCredential;
import org.jboss.workspace.client.security.impl.PasswordCredential;

import java.util.HashMap;
import java.util.Map;

public class SecurityManager {
    public static void doAuthentication(final String name, final AuthenticationHandler handler) {

        final String responseSubject = "org.jboss.workspace.authentication." + name;
        MessageBusClient.subscribe(responseSubject, new AcceptsCallback() {
            public void callback(Object message, Object data) {
                Map<String, Object> msgParts = MessageBusClient.decodeMap(message);
                String commandType = (String) msgParts.get(CommandType.name());

                switch (SecurityCommands.valueOf(commandType)) {
                    case WhatCredentials:
                        String credentialsRequired = (String) msgParts.get(CredentialsRequired.name());
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
                        System.out.println("GOT RESPONSE");
                        break;
                        
                }
            }
        });





    }

}
