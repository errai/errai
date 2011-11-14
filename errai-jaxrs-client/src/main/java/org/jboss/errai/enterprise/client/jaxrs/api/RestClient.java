package org.jboss.errai.enterprise.client.jaxrs.api;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.framework.ProxyProvider;
import org.jboss.errai.bus.client.framework.RPCStub;
import org.jboss.errai.bus.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsProxyLoader;

import com.google.gwt.core.client.GWT;

/**
 * API for executing HTTP calls based on a JAX-RS resource.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RestClient {

  // TODO The proxy factory is shared with Errai RPC for now
  private static ProxyProvider proxyProvider = new RemoteServiceProxyFactory();

  /**
   * Creates a REST client for the provided JAX-RS resource class/interface.
   * 
   * @param callback  the asynchronous callback to use
   * @param remoteService  the remote service class or interface
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, final RemoteCallback<R> callback) {
    return create(remoteService, callback, null);
  }

  /**
   * Creates a REST client for the provided JAX-RS resource class/interface.
   * 
   * @param callback  the asynchronous callback to use 
   * @param errorCallback  the error callback to use
   * @param remoteService  the remote service class or interface
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, 
      final RemoteCallback<R> callback, final ErrorCallback errorCallback) {
    
    T svc = proxyProvider.getRemoteProxy(remoteService);
    if (svc == null) {

      // double check that the extensions loader has been bootstrapped
      JaxrsProxyLoader loader = GWT.create(JaxrsProxyLoader.class);
      loader.loadProxies();

      if (proxyProvider.getRemoteProxy(remoteService) == null)
        throw new RuntimeException("No proxy found for JAX-RS interface: " + remoteService.getName());
      else
        return create(remoteService, callback, errorCallback);
    }

    ((RPCStub) svc).setRemoteCallback(callback);
    ((RPCStub) svc).setErrorCallback(errorCallback);
    return svc;
  }
}