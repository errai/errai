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
     * @throws Exception throws an InterruptedException specifically, if the thread is interrupted while trying
     *                   to offer a message to the worker queue. This isn't specifically exposed here due
     *                   to the fact that InterruptedException is not exposed to the GWT client library.
     */
    public void dispatchGlobal(Message message) throws Exception;

    /**
     * Dispatches a message to a single receiver on the bus
     *
     * @param message - a message to dispatch
     * @throws Exception throws an InterruptedException specifically, if the thread is interrupted while trying
     *                   to offer a message to the worker queue. This isn't specifically exposed here due
     *                   to the fact that InterruptedException is not exposed to the GWT client library.
     */
    public void dispatch(Message message) throws Exception;
}
