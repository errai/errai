package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.BooleanRoutingRule;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.Payload;

public interface ServerMessageBus extends MessageBus {
    public Payload nextMessage(Object sessionContext);
    public void addRule(String subject, BooleanRoutingRule rule);
}
