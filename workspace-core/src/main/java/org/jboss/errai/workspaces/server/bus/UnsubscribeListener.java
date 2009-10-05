package org.jboss.errai.workspaces.server.bus;

public interface UnsubscribeListener {
    public void onUnsubscribe(SubscriptionEvent event);
}
