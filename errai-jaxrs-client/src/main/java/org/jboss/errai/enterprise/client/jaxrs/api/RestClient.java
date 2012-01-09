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
import org.jboss.errai.bus.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsProxy;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsProxyLoader;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;

import com.google.gwt.core.client.GWT;

/**
 * API for executing HTTP calls based on a JAX-RS interface.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RestClient {
  static {
    // ensure that the marshalling framework has been initialized
    MarshallerFramework.initializeDefaultSessionProvider();
  }
  
  private static ProxyProvider proxyProvider = new RemoteServiceProxyFactory();

  /**
   * Creates a REST client for the provided JAX-RS resource interface.
   *
   * @param remoteService  the JAX-RS interface
   * @param callback  the asynchronous callback to use
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, final RemoteCallback<R> callback) {
    return create(remoteService, null, callback, null);
  }

  /**
   * Creates a REST client for the provided JAX-RS resource interface.
   * 
   * @param remoteService  the JAX-RS interface
   * @param baseUrl  the base url overriding the default application root path
   * @param callback  the asynchronous callback to use
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, String baseUrl, final RemoteCallback<R> callback) {
    return create(remoteService, baseUrl, callback, null);
  }
  
  /**
   * Creates a REST client for the provided JAX-RS resource interface.
   * 
   * @param remoteService  the JAX-RS interface
   * @param callback  the asynchronous callback to use 
   * @param errorCallback  the error callback to use
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, 
      final RemoteCallback<R> callback, final ErrorCallback errorCallback) {
    return create(remoteService, null, callback, errorCallback);
  }
  
  /**
   * Creates a REST client for the provided JAX-RS resource interface.
   * 
   * @param remoteService  the JAX-RS interface
   * @param baseUrl  the base url overriding the default application root path
   * @param callback  the asynchronous callback to use
   * @param errorCallback  the error callback to use
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, String baseUrl,
      final RemoteCallback<R> callback, final ErrorCallback errorCallback) {

    if (baseUrl != null && !baseUrl.endsWith("/")) 
      baseUrl += "/";
    
    T proxy = proxyProvider.getRemoteProxy(remoteService);
    if (proxy == null || !(proxy instanceof JaxrsProxy)) {

      JaxrsProxyLoader loader = GWT.create(JaxrsProxyLoader.class);
      loader.loadProxies();

      proxy = proxyProvider.getRemoteProxy(remoteService); 
      if (proxy == null || !(proxy instanceof JaxrsProxy))
        throw new RuntimeException("No proxy found for JAX-RS interface: " + remoteService.getName());
    }

    ((JaxrsProxy) proxy).setRemoteCallback(callback);
    ((JaxrsProxy) proxy).setErrorCallback(errorCallback);
    ((JaxrsProxy) proxy).setBaseUrl(baseUrl);
    return proxy;
  }
  
  
  /**
   * Returns the configured JAX-RS default application root path.
   * 
   * @return path with trailing slash, or empty string if undefined or explicitly set to empty
   */
  public static native String getApplicationRoot() /*-{
    if ($wnd.erraiJaxRsApplicationRoot === undefined || $wnd.erraiJaxRsApplicationRoot.length === 0) {
      return ""; 
    } 
    else {
      if ($wnd.erraiJaxRsApplicationRoot.substr(-1) !== "/") {
        return $wnd.erraiJaxRsApplicationRoot + "/";
      }
      return $wnd.erraiJaxRsApplicationRoot;
    }
  }-*/;
  
  /**
   * Configures the JAX-RS default application root path.
   * 
   * @param root path to use when sending request to the endpoint
   */
  public static native void setApplicationRoot(String path) /*-{
    if (path == null) {
      $wnd.erraiJaxRsApplicationRoot = undefined;
    }
    else {
      $wnd.erraiJaxRsApplicationRoot = path;
    }
  }-*/;
}