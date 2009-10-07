package org.jboss.errai.bus.client;

public interface BooleanRoutingRule {
    public boolean decision(CommandMessage message);
}
