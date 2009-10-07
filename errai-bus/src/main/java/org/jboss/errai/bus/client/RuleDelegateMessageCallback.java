package org.jboss.errai.bus.client;

public class RuleDelegateMessageCallback implements MessageCallback {
    private MessageCallback delegate;
    private BooleanRoutingRule routingRule;

    public RuleDelegateMessageCallback(MessageCallback delegate, BooleanRoutingRule rule) {
        this.delegate = delegate;
        this.routingRule = rule;
    }

    public void callback(CommandMessage message) {
        if (routingRule.decision(message)) {
            this.delegate.callback(message);
        }
    }
}
