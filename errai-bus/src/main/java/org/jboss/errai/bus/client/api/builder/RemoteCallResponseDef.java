package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.RemoteCallback;


public interface RemoteCallResponseDef {
    public RemoteCallErrorDef respondTo(RemoteCallback callback);

    public <T> RemoteCallErrorDef respondTo(Class<T> returnType, RemoteCallback<T> callback);
}
