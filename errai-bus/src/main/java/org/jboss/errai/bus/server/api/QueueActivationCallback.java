package org.jboss.errai.bus.server.api;

/**
 * This interface, <tt>QueueActivationCallback</tt>, is a template for creating a callback function for a queue
 */
public interface QueueActivationCallback {

    /**
     * This function is responsible for activating a queue. It starts the message transmission
     *
     * @param queue - the message queue to be activated
     */
    public void activate(MessageQueue queue);
}
