/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.enterprise.client.jaxrs.api;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.framework.ProxyFactory;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.enterprise.client.jaxrs.AbstractJaxrsProxy;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Cookies;

/**
 * API for communicating with REST endpoints based on JAX-RS interfaces.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RestClient {
  private static ProxyFactory proxyProvider = new RemoteServiceProxyFactory();

  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService
   *          the JAX-RS resource interface. Must not be null.
   * @param callback
   *          the asynchronous callback to use. Must not be null.
   * @param successCodes
   *          optional HTTP status codes used to determine whether the request
   *          was successful. If omitted, all 2xx status codes are interpreted
   *          as success for this request.
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, final RemoteCallback<R> callback, Integer... successCodes) {
    return create(remoteService, null, callback, null, successCodes);
  }

  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService
   *          the JAX-RS resource interface. Must not be null.
   * @param callback
   *          the asynchronous callback to use. Must not be null.
   * @param requestCallback
   *          the request callback that provides access to the
   *          {@link com.google.gwt.http.client.Request}.
   * @param successCodes
   *          optional HTTP status codes used to determine whether the request
   *          was successful. If omitted, all 2xx status codes are interpreted
   *          as success for this request.
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, final RemoteCallback<R> callback,
          final RequestCallback requestCallback, Integer... successCodes) {
    return create(remoteService, null, callback, null, requestCallback, successCodes);
  }

  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService
   *          the JAX-RS resource interface. Must not be null.
   * @param baseUrl
   *          the base URL overriding the default application root path
   * @param callback
   *          the asynchronous callback to use. Must not be null.
   * @param successCodes
   *          optional HTTP status codes used to determine whether the request
   *          was successful. If omitted, all 2xx status codes are interpreted
   *          as success for this request.
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, String baseUrl, final RemoteCallback<R> callback,
          Integer... successCodes) {
    return create(remoteService, baseUrl, callback, null, successCodes);
  }

  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService
   *          the JAX-RS resource interface. Must not be null.
   * @param callback
   *          the asynchronous callback to use. Must not be null.
   * @param errorCallback
   *          the error callback to use
   * @param successCodes
   *          optional HTTP status codes used to determine whether the request
   *          was successful. If omitted, all 2xx status codes are interpreted
   *          as success for this request.
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, final RemoteCallback<R> callback,
          final RestErrorCallback errorCallback, Integer... successCodes) {
    return create(remoteService, null, callback, errorCallback, successCodes);
  }

  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService
   *          the JAX-RS resource interface. Must not be null.
   * @param callback
   *          the asynchronous callback to use. Must not be null.
   * @param errorCallback
   *          the error callback to use
   * @param requestCallback
   *          the request callback that provides access to the
   *          {@link com.google.gwt.http.client.Request}.
   * @param successCodes
   *          optional HTTP status codes used to determine whether the request
   *          was successful. If omitted, all 2xx status codes are interpreted
   *          as success for this request.
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, final RemoteCallback<R> callback,
          final RestErrorCallback errorCallback, final RequestCallback requestCallback, Integer... successCodes) {
    return create(remoteService, null, callback, errorCallback, requestCallback, successCodes);
  }

  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService
   *          the JAX-RS resource interface. Must not be null.
   * @param baseUrl
   *          the base URL overriding the default application root path
   * @param callback
   *          the asynchronous callback to use. Must not be null.
   * @param errorCallback
   *          the error callback to use
   * @param successCodes
   *          optional HTTP status codes used to determine whether the request
   *          was successful. If omitted, all 2xx status codes are interpreted
   *          as success for this request.
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, String baseUrl, final RemoteCallback<R> callback,
          final RestErrorCallback errorCallback, Integer... successCodes) {
    return create(remoteService, baseUrl, callback, errorCallback, null, successCodes);
  }

  /**
   * Creates a client/proxy for the provided JAX-RS resource interface.
   * 
   * @param remoteService
   *          the JAX-RS resource interface. Must not be null.
   * @param baseUrl
   *          the base URL overriding the default application root path
   * @param callback
   *          the asynchronous callback to use. Must not be null.
   * @param errorCallback
   *          the error callback to use
   * @param requestCallback
   *          the request callback that provides access to the
   *          {@link com.google.gwt.http.client.Request}.
   * @param successCodes
   *          optional HTTP status codes used to determine whether the request
   *          was successful. If omitted, all 2xx status codes are interpreted
   *          as success for this request.
   * @return proxy of the specified remote service type
   */
  public static <T, R> T create(final Class<T> remoteService, String baseUrl, final RemoteCallback<R> callback,
          final RestErrorCallback errorCallback, final RequestCallback requestCallback, Integer... successCodes) {

    Assert.notNull(remoteService);
    Assert.notNull(callback);
    if (baseUrl != null && !baseUrl.endsWith("/"))
      baseUrl += "/";

    T proxy = proxyProvider.getRemoteProxy(remoteService);

    if (successCodes.length > 0) {
      ((AbstractJaxrsProxy) proxy).setSuccessCodes(Lists.newArrayList(successCodes));
    }

    ((AbstractJaxrsProxy) proxy).setRemoteCallback(callback);
    ((AbstractJaxrsProxy) proxy).setErrorCallback(errorCallback);
    ((AbstractJaxrsProxy) proxy).setRequestCallback(requestCallback);
    ((AbstractJaxrsProxy) proxy).setBaseUrl(baseUrl);
    return proxy;
  }

  /**
   * Returns the configured JAX-RS default application root path.
   * 
   * @return path with trailing slash, or empty string if undefined or
   *         explicitly set to empty
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
   * @param path
   *          path to use when sending requests to the JAX-RS endpoint
   */
  public static native void setApplicationRoot(String path) /*-{
    if (path == null) {
      $wnd.erraiJaxRsApplicationRoot = undefined;
    }
    else {
      $wnd.erraiJaxRsApplicationRoot = path;
    }
  }-*/;

  /**
   * Checks if a jackson compatible JSON format should be used instead of Errai
   * JSON.
   * 
   * @return true, if jackson marshalling should be used, otherwise false.
   */
  public static native boolean isJacksonMarshallingActive() /*-{
    if ($wnd.erraiJaxRsJacksonMarshallingActive === undefined) {
      return false; 
    } 
    else {
      return $wnd.erraiJaxRsJacksonMarshallingActive;
    }
  }-*/;

  /**
   * Activates/Deactivates jackson conform JSON marshalling.
   * 
   * @param active
   *          true if jackson marshalling should be activated, otherwise false.
   */
  public static native void setJacksonMarshallingActive(boolean active) /*-{
    $wnd.erraiJaxRsJacksonMarshallingActive = active;
  }-*/;

  /**
   * Set a cookie for the domain and path returned by
   * {@link #getApplicationRoot()}.
   * 
   * @param cookieName
   *          The name of the cookie to set.
   * @param cookieValue
   *          The value of the cookie to set.
   */
  public static void setCookie(final String cookieName, final String cookieValue) {
    final AnchorElement anchorElement = Document.get().createAnchorElement();
    anchorElement.setHref(RestClient.getApplicationRoot());
    final String path = anchorElement.getPropertyString("pathname");
    final String domain = anchorElement.getPropertyString("domain");

    Cookies.setCookie(cookieName, cookieValue, null, domain, path, false);
  }
}
