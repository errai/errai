package org.jboss.errai.bus.server.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.api.ServerMessageBus;

/**
 * @author Mike Brock
 */
public class ErraiServiceFactory {
  public static ErraiService create(final ErraiServiceConfigurator configurator) {
    return Guice.createInjector(new AbstractModule() {
      public void configure() {
        bind(MessageBus.class).to(ServerMessageBusImpl.class);
        bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
        bind(ErraiService.class).to(ErraiServiceImpl.class);
        bind(ErraiServiceConfigurator.class).toInstance(configurator);
      }
    }).getInstance(ErraiService.class);
  }
}
