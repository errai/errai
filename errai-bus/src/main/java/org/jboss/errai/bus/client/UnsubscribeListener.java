package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.SubscriptionEvent;

public interface UnsubscribeListener {
    public void onUnsubscribe(SubscriptionEvent event);
}
