package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.RemoteCallback;

/**
 * This interface, <tt>RemoteCallServiceDef</tt> is a template for setting a service for a remote call. It ensures that
 * it is constructed properly
 */
public interface RemoteCallServiceDef {

    /**
     * Calls a service
     *
     * @param serviceName - the service to be called
     * @return an instance of <tt>RemoteCallEndpointDef</tt>
     */
    public RemoteCallEndpointDef call(String serviceName);
}