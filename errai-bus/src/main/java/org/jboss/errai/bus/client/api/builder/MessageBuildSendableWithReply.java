package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.MessageCallback;

public interface MessageBuildSendableWithReply extends MessageBuildSendable {
    public MessageBuildSendable repliesTo(MessageCallback callback);
}
