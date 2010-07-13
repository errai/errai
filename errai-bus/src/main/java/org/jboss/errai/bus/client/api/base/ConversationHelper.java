package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.bus.client.protocols.MessageParts;

public class ConversationHelper {
    private static final String RES_NAME = "MessageReplyCallback";

    public static void makeConversational(Message message, MessageCallback callback) {
        message.setResource(RES_NAME, callback);
        message.setFlag(RoutingFlags.Conversational);
    }

    public static void createConversationService(MessageBus bus, Message m) {
        if (m.isFlagSet(RoutingFlags.Conversational)) {
            final String replyService = m.getSubject() + ":RespondTo:" + count();
            bus.subscribe(replyService, m.getResource(MessageCallback.class, RES_NAME));
            bus.subscribe(replyService, new ServiceCanceller(replyService, bus));
            m.set(MessageParts.ReplyTo, replyService);
        }
    }

    static int counter = 0;

    static int count() {
        if (++counter > 1000) {
            return counter = 0;
        }
        return counter;
    }
}
