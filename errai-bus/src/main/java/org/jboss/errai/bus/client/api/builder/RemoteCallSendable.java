package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.framework.MessageBus;

/**
 * This interface, <tt>RemoteCallSendable</tt> is a template for sending a message remotely. It ensures that
 * it is constructed properly
 */
public interface RemoteCallSendable {

    /**
     * Specifies how to send the message
     *
     * @param viaThis - the message bus to send the message with
     */
    public void sendNowWith(MessageBus viaThis);
}
