package org.jboss.errai.ioc.client.api.builtin;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.ioc.client.api.Provider;
import org.jboss.errai.ioc.client.api.TypeProvider;

import javax.inject.Singleton;

@Provider
@Singleton
public class MessageBusProvider implements TypeProvider<MessageBus> {
    public MessageBus provide() {
        return ErraiBus.get();
    }
}
