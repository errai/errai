package org.jboss.errai.bus.client.api.builder;

public interface RemoteCallEndpointDef {
    public RemoteCallResponseDef endpoint(String endPointName);
    public RemoteCallResponseDef endpoint(String endPointName, Object... args);
}
