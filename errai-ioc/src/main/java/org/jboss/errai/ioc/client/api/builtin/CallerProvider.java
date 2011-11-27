package org.jboss.errai.ioc.client.api.builtin;

import org.jboss.errai.bus.client.api.Caller;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.framework.RPCStub;
import org.jboss.errai.bus.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
@IOCProvider
public class CallerProvider implements ContextualTypeProvider<Caller<?>> {
  private static final RemoteServiceProxyFactory factory = new RemoteServiceProxyFactory();

  @Override
  public Caller<?> provide(final Class[] typeargs, Annotation[] qualifiers) {
    return new Caller<Object>() {
      @Override
      public Object call(RemoteCallback<?> callback) {
        Object proxy = factory.getRemoteProxy(typeargs[0]);
        ((RPCStub) proxy).setRemoteCallback(callback);
        return proxy;
      }

      @Override
      public Object call(ErrorCallback errorCallback) {
        Object proxy = factory.getRemoteProxy(typeargs[0]);
        ((RPCStub) proxy).setErrorCallback(errorCallback);
        return proxy;
      }

      @Override
      public Object call(RemoteCallback<?> callback, ErrorCallback errorCallback) {
        Object proxy = factory.getRemoteProxy(typeargs[0]);
        ((RPCStub) proxy).setRemoteCallback(callback);
        ((RPCStub) proxy).setErrorCallback(errorCallback);
        return proxy;
      }
    };
  }
}
