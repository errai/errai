package org.jboss.errai.bus.server.service;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.api.SessionProvider;

import java.util.Collection;

/**
 * @author Mike Brock
 */
class ErraiServiceProxy implements ErraiService<Object> {

  private MessageBusProxy messageBusProxy = new MessageBusProxy();
  private RequestDispatcherProxy requestDispatcherProxy = new RequestDispatcherProxy();
  private ErraiService service;

  @Override
  public void store(Message message) {
    service.store(message);
  }

  @Override
  public void store(Collection messages) {
    service.store(messages);
  }

  @Override
  public ServerMessageBus getBus() {
    return messageBusProxy;
  }

  @Override
  public ErraiServiceConfigurator getConfiguration() {
    return service.getConfiguration();
  }

  @Override
  public void addShutdownHook(Runnable runnable) {
    service.addShutdownHook(runnable);
  }

  @Override
  public void stopService() {
    service.stopService();
  }

  @Override
  public SessionProvider getSessionProvider() {
    return service.getSessionProvider();
  }

  @Override
  public void setSessionProvider(SessionProvider sessionProvider) {
    throw new IllegalStateException("cannot set session provider in proxy");

  }

  @Override
  public RequestDispatcher getDispatcher() {
    return requestDispatcherProxy;
  }

  @Override
  public void setDispatcher(RequestDispatcher dispatcher) {
    throw new IllegalStateException("cannot set dispatcher in proxy");
  }

  public void closeProxy(ErraiService service) {
    this.service = service;
    messageBusProxy.closeProxy(service.getBus());
    requestDispatcherProxy.closeProxy(service.getDispatcher());
  }
}
