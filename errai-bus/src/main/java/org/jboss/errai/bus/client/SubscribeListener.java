package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.SubscriptionEvent;

public interface SubscribeListener {
    public void onSubscribe(SubscriptionEvent event);
}
