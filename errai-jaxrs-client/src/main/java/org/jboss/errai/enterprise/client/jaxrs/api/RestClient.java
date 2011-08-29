package org.jboss.errai.enterprise.client.jaxrs.api;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.framework.ProxyProvider;
import org.jboss.errai.bus.client.framework.RPCStub;
import org.jboss.errai.bus.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsExtensionsLoader;

import com.google.gwt.core.client.GWT;

/**
 * API to execute REST calls based on a JAX-RS interface.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RestClient {

  // TODO The proxy factory is shared with Errai RPC for now
  private static ProxyProvider proxyProvider = new RemoteServiceProxyFactory();

  public static <T, R> T createCall(final RemoteCallback<R> callback, final Class<T> remoteService) {
    return createCall(callback, null, remoteService);
  }

  public static <T, R> T createCall(final RemoteCallback<R> callback, final ErrorCallback errorCallback,
      final Class<T> remoteService) {
    
    T svc = proxyProvider.getRemoteProxy(remoteService);
    if (svc == null) {

      // double check that the extensions loader has been bootstrapped
      GWT.create(JaxrsExtensionsLoader.class);

      if (proxyProvider.getRemoteProxy(remoteService) == null)
        throw new RuntimeException("No proxy found for JAX-RS interface: " + remoteService.getName());
      else
        return createCall(callback, errorCallback, remoteService);
    }

    ((RPCStub) svc).setRemoteCallback(callback);
    ((RPCStub) svc).setErrorCallback(errorCallback);
    return svc;
  }
}