package org.jboss.errai.bus.client.framework;

public interface ProxyProvider {
    public <T> T getRemoteProxy(Class<T> proxyType); 
}
