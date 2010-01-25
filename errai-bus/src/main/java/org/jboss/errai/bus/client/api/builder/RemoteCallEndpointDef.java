package org.jboss.errai.bus.client.api.builder;

/**
 * This interface, <tt>RemoteCallEndpointDef</tt> is a template for creating a remote call endpoint. It ensures that
 * the endpoint is constructed properly
 */
public interface RemoteCallEndpointDef {

    /**
     * Sets the endpoint for a message using the specified name
     *
     * @param endPointName - name of endpoint
     * @return an instance of <tt>RemoteCallResponseDef</tt>
     */
    public RemoteCallResponseDef endpoint(String endPointName);

    /**
     * Sets the endpoint for a message using the specified name
     *
     * @param endPointName - name of endpoint
     * @param args - the parameters for the endpoint function 
     * @return an instance of <tt>RemoteCallResponseDef</tt>
     */
    public RemoteCallResponseDef endpoint(String endPointName, Object... args);
}
