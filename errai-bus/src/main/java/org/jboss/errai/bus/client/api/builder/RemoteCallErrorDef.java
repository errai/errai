package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.ErrorCallback;

public interface RemoteCallErrorDef {
    public RemoteCallSendable errorsHandledBy(ErrorCallback errorCallback);
    public RemoteCallSendable noErrorHandling();
}
