package org.jboss.errai.bus.server.service;

import org.jboss.errai.workspaces.client.bus.CommandMessage;
import org.jboss.errai.bus.server.bus.MessageBus;

public interface ErraiService {
    public static final String AUTHORIZATION_SVC_SUBJECT = "AuthorizationService";

    public void store(CommandMessage message);
    public MessageBus getBus();
}
