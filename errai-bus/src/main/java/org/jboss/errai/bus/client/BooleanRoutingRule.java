package org.jboss.errai.bus.client;

/**
 * A RoutingRule that is called by the bus before routing any message.
 */
public interface BooleanRoutingRule {
    public boolean decision(CommandMessage message);
}
