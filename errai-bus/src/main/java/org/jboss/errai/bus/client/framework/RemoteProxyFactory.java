package org.jboss.errai.bus.client.framework;

import java.util.HashMap;
import java.util.Map;

public class RemoteProxyFactory {
    private static Map<Class, Object> remoteProxies = new HashMap<Class, Object>();

    public static <T> T getRemoteProxy(Class<T> proxyType) {
        return (T) remoteProxies.get(proxyType);
    }

    public static void addRemoteProxy(Class proxyType, Object proxy) {
        remoteProxies.put(proxyType, proxy);
    }
}
