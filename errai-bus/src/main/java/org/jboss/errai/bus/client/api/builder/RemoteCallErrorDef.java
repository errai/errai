package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.ErrorCallback;

/**
 * This interface, <tt>RemoteCallErrorDef</tt> is a template for creating a remote call error handler. It ensures that
 * the error is constructed properly
 */
public interface RemoteCallErrorDef {

    /**
     * Sets the error handler function and returns an instance of <tt>RemoteCallSendable</tt>
     *
     * @param errorCallback - the error handler
     * @return an instance of <tt>RemoteCallSendable</tt>
     */
    public RemoteCallSendable errorsHandledBy(ErrorCallback errorCallback);

    /**
     * If this function is called, it means that no there is no error handler, and only returns an instance of
     * <tt>RemoteCallSendable</tt>
     *
     * @return an instance of <tt>RemoteCallSendable</tt>
     */
    public RemoteCallSendable noErrorHandling();
}
