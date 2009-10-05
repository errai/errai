package org.jboss.errai.bus.server.bus;

public interface UnsubscribeListener {
    public void onUnsubscribe(SubscriptionEvent event);
}
