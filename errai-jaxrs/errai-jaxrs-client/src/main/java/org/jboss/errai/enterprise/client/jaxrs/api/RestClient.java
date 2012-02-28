/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.framework.ProxyFactory;
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
  
  private static ProxyFactory proxyProvider = new RemoteServiceProxyFactory();

  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   *
   * @param remoteService  the JAX-RS interface
   * @param callback  the asynchronous callback to use
   * @param successCodes  optional HTTP status codes used to determine if the request was successful
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, final RemoteCallback<R> callback,
      int... successCodes) {
    return create(remoteService, null, callback, null, successCodes);
  }

  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService  the JAX-RS interface
   * @param baseUrl  the base url overriding the default application root path
   * @param callback  the asynchronous callback to use
   * @param successCodes  optional HTTP status codes used to determine if the request was successful
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, String baseUrl, final RemoteCallback<R> callback, 
      int... successCodes) {
    return create(remoteService, baseUrl, callback, null, successCodes);
  }
  
  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService  the JAX-RS interface
   * @param callback  the asynchronous callback to use 
   * @param errorCallback  the error callback to use
   * @param successCodes  optional HTTP status codes used to determine if the request was successful
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, final RemoteCallback<R> callback,
      final ErrorCallback errorCallback, int... successCodes) {
    return create(remoteService, null, callback, errorCallback, successCodes);
  }
  
  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService  the JAX-RS interface
   * @param baseUrl  the base url overriding the default application root path
   * @param callback  the asynchronous callback to use
   * @param errorCallback  the error callback to use
   * @param successCodes  optional HTTP status codes used to determine if the request was successful
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, String baseUrl, final RemoteCallback<R> callback, 
      final ErrorCallback errorCallback, int... successCodes) {

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

    // Can't use ArrayUtils (class has to be translatable).
    if (successCodes.length > 0) {
      List<Integer> codes = new ArrayList<Integer>();
      for (int code : successCodes) {
        codes.add(code);
      }
      ((JaxrsProxy) proxy).setSuccessCodes(codes);
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
   * @param root path to use when sending requests to the JAX-RS endpoint
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