package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.MessageBus;

public interface RemoteCallSendable {
    public void sendNowWith(MessageBus viaThis);
}
