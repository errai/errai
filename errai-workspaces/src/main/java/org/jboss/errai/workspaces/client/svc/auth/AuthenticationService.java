/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.errai.workspaces.client.svc.auth;

import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.workspaces.client.AbstractLayout;
import org.jboss.errai.workspaces.client.protocols.LayoutCommands;
import org.jboss.errai.workspaces.client.svc.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Upon successful login, sends a message to 'appContext:username'.
 */
public class AuthenticationService implements Service {
    protected AuthenticationPresenter authenticationHandler;

    private List<String> sessionRoles = new ArrayList<String>();

    public AuthenticationService() {
        authenticationHandler = new AuthenticationPresenter(new MosaicAuthenticationDisplay());
        sessionRoles = new ArrayList<String>();
    }

    @Override
    public void start() {
        final ClientMessageBus bus = (ClientMessageBus) ErraiBus.get();

        bus.subscribe("LoginClient", authenticationHandler);

        // negotiate login
        if (bus.isInitialized()) {
            authenticationHandler.getNegotiationTask().run();
        } else {
            bus.addPostInitTask(authenticationHandler.getNegotiationTask());
        }

        // This service is used for setting up and restoring the session.
        bus.subscribe("ClientConfiguratorService",
                new MessageCallback() {
                    public void callback(Message message) {


                        if (message.hasPart(SecurityParts.Roles)) {
                            String[] roleStrs = message.get(String.class, SecurityParts.Roles).split(",");
                            for (String s : roleStrs) {
                                sessionRoles.add(s.trim());
                            }
                        }

                        if (message.hasPart(SecurityParts.Name)) {
                            String username = message.get(String.class, SecurityParts.Name);

                            MessageBuilder.createMessage()
                                    .toSubject(AbstractLayout.WORKSPACE_SVC)
                                    .command(LayoutCommands.Initialize)
                                    .noErrorHandling()
                                    .sendNowWith(ErraiBus.get());


                        }
                    }
                });
    }

    @Override
    public void stop() {

    }

    public List<String> getSessionRoles() {
        return sessionRoles;
    }
}
