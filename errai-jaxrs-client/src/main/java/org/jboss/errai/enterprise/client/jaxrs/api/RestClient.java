/*
 * Copyright 2011 JBoss, a division of Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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