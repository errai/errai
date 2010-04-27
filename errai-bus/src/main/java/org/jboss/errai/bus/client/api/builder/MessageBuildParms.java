package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.ResourceProvider;

/**
 * This interface, <tt>MessageBuildParms</tt>, is a template for building the different parameters of a message. This
 * ensures that they are constructed properly
 */
public interface MessageBuildParms<R> extends MessageBuild {

    /**
     * Sets the message part to the specified value
     *
     * @param part - the message part
     * @param value - the value of the message part
     * @return the updated instance of <tt>MessageBuildParms</tt>
     */
    public MessageBuildParms<R> with(String part, Object value);

    /**
     * Sets the message part to the specified value
     *
     * @param part - the message part
     * @param value - the value of the message part
     * @return the updated instance of <tt>MessageBuildParms</tt>
     */
    public MessageBuildParms<R> with(Enum part, Object value);


    public MessageBuildParms<R> withProvided(String part, ResourceProvider provider);

    public MessageBuildParms<R> withProvided(Enum part, ResourceProvider provider);

    /**
     * Copies the message part to the specified message
     *
     * @param part - the message part
     * @param m - the message
     * @return the updated instance of <tt>MessageBuildParms</tt>
     */
    public MessageBuildParms<R> copy(String part, Message m);

    /**
     * Copies the message part to the specified message
     *
     * @param part - the message part
     * @param m - the message
     * @return the updated instance of <tt>MessageBuildParms</tt>
     */
    public MessageBuildParms<R> copy(Enum part, Message m);

    /**
     * Copies the message resource to the specified message
     *
     * @param part - the message resource
     * @param m - the message
     * @return the updated instance of <tt>MessageBuildParms</tt>
     */
    public MessageBuildParms<R> copyResource(String part, Message m);

    /**
     * Sets the error callback function for the message
     *
     * @param callback - the callback function called if an error occurs
     * @return -
     */
    public R errorsHandledBy(ErrorCallback callback);

    /**
     * Specifies that the message's errors will not be handled
     *
     * @return -
     */
    public R noErrorHandling();

    /**
     * Use the default error handler.
     * @return -
     */
    public R defaultErrorHandling();
}
