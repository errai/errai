package org.jboss.errai.bus.client.framework;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;

public interface RPCStub {
    public void setRemoteCallback(RemoteCallback callback);
    public void setErrorCallback(ErrorCallback callback);
}
