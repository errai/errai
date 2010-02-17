package org.jboss.errai.bus.client.framework;

import org.jboss.errai.bus.client.api.Message;

/**
 * The <tt>RequestDispatcher</tt> interface provides a way to create a message delivery system into the bus
 */
public interface RequestDispatcher {

    /**
     * Dispatches a message to all global listeners on the bus
     *
     * @param message - a message to dispatch globally
     */
    public void dispatchGlobal(Message message);

    /**
     * Dispatches a message to a single receiver on the bus
     *
     * @param message - a message to dispatch
     */
    public void dispatch(Message message);
}
