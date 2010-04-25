package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.base.AsyncTask;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;


/**
 * This interface, <tt>MessageBuildSendable</tt>, is a template for sending a message. This ensures that the message is
 * constructed properly
 */
public interface MessageBuildSendable extends Sendable {

    /**
     * Sends the message with the specified <tt>MessageBus</tt>
     *
     * @param viaThis - the message bus to send the message with
     */
    public void sendNowWith(MessageBus viaThis);

    /**
     * Sends the message with the specified <tt>MessageBus</tt>
     *
     * @param viaThis - the message bus to send the message with
     * @param fireMessageListener - true if the message listeners should be notified
     */
    public void sendNowWith(MessageBus viaThis, boolean fireMessageListener);

    /**
     * Sends the message with the specified <tt>RequestDispatcher</tt>
     *
     * @param viaThis - the dispatcher to send the message with
     */
    public void sendNowWith(RequestDispatcher viaThis);


    public AsyncTask sendRepeatingWith(RequestDispatcher viaThis, TimeUnit unit, int millis);

    public AsyncTask sendDelayedWith(RequestDispatcher viaThis, TimeUnit unit, int millis);

}
