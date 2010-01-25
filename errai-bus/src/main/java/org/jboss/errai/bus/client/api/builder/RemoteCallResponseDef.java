package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.RemoteCallback;

/**
 * This interface, <tt>RemoteCallResponseDef</tt> is a template for creating a remote call response. It ensures that
 * the response point is constructed properly
 */
public interface RemoteCallResponseDef {

    /**
     * Sets the callback response function, which is called after an endpoint is reached
     *
     * @param callback - the callback function
     * @return an instance of <tt>RemoteCallErrorDef</tt>
     */
    public RemoteCallErrorDef respondTo(RemoteCallback callback);

    /**
     * Sets the callback response function, which is called after an endpoint is reached
     *
     * @param returnType - the return type of the callback function
     * @param callback - the callback function
     * @return an instance of <tt>RemoteCallErrorDef</tt>
     */
    public <T> RemoteCallErrorDef respondTo(Class<T> returnType, RemoteCallback<T> callback);
}
