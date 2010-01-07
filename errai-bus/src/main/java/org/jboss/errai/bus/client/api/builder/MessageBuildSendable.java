package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.RequestDispatcher;

public interface MessageBuildSendable extends MessageBuild {
    public void sendNowWith(MessageBus viaThis);

    public void sendNowWith(MessageBus viaThis, boolean fireMessageListener);

    public void sendNowWith(RequestDispatcher viaThis);
}
