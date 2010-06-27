package org.jboss.errai.ioc.client.api.builtin;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.ioc.client.api.Provider;
import org.jboss.errai.ioc.client.api.TypeProvider;

@Provider
public class RequestDispatcherProvider implements TypeProvider<RequestDispatcher> {
    public RequestDispatcher provide() {
        return ErraiBus.getDispatcher();
    }
}
