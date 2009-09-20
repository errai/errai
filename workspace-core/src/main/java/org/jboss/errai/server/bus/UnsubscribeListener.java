package org.jboss.errai.server.bus;

public interface UnsubscribeListener {
    public void onUnsubscribe(SubscriptionEvent event);
}
