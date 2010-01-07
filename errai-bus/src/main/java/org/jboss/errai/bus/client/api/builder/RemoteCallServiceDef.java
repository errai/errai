package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.RemoteCallback;

public interface RemoteCallServiceDef {
    public RemoteCallEndpointDef call(String serviceName);
}