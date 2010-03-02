package org.jboss.errai.bus.client.framework;

import org.jboss.errai.bus.client.api.RemoteCallback;

public interface RPCWrapper extends RPCStub {
    public Object getWrapped();
    public RemoteCallback getRemoteCallback();
}
