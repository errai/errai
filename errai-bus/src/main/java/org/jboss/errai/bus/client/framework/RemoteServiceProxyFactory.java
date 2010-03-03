package org.jboss.errai.bus.client.framework;

import java.util.HashMap;
import java.util.Map;

public class RemoteServiceProxyFactory implements ProxyProvider {
    private static Map<Class, Object> remoteProxies = new HashMap<Class, Object>();

    public Object getRemoteProxy(Class proxyType) {
        return remoteProxies.get(proxyType);
    }

    public static void addRemoteProxy(Class proxyType, Object proxy) {
        remoteProxies.put(proxyType, proxy);
    }
}
