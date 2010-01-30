package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.HasEncoded;
import org.jboss.errai.bus.client.Message;


public class HasEncodedConvMessageWrapper extends ConversationMessageWrapper implements HasEncoded {
    public HasEncodedConvMessageWrapper(Message inReplyTo, Message newMessage) {
        super(inReplyTo, newMessage);
    }

    public String getEncoded() {
        return ((HasEncoded) newMessage).getEncoded();
    }
}
