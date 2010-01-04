package org.jboss.errai.bus.client;

public interface RemoteCallback<T> {
    public void callback(T response);
}
