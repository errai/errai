package org.jboss.errai.server.service;

import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.server.bus.MessageBus;

public interface ErraiService {
    public static final String AUTHORIZATION_SVC_SUBJECT = "AuthorizationService";

    public void store(CommandMessage message);
    public MessageBus getBus();
}
