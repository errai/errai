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

package org.jboss.errai.enterprise.client.jaxrs;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.framework.ClientCSRFTokenCache;
import org.jboss.errai.common.client.framework.Constants;
import org.jboss.errai.common.client.framework.RpcBatch;
import org.jboss.errai.common.client.framework.RpcStub;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * JAX-RS proxies are {@link RpcStub}s managed by the shared {@see
 * RemoteServiceProxyFactory}. The implementations of this class are generated
 * at compile time.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractJaxrsProxy implements RpcStub {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private String baseUrl;
  private List<Integer> successCodes;
  private ClientExceptionMapper exceptionMapper;
  private RemoteCallback<Request> requestCallback;

  /**
   * Returns the remote callback used by this proxy.
   *
   * @return the remote callback, never null.
   */
  public abstract RemoteCallback<?> getRemoteCallback();

  /**
   * Returns the error callback used by this proxy.
   *
   * @return the error callback, null if no error callback was provided.
   */
  public abstract ErrorCallback<?> getErrorCallback();

  /**
   * If not set explicitly, the base URL is the configured default application
   * root path {@see RestClient}.
   *
   * @return the base URL used to contact the remote service
   */
  public String getBaseUrl() {
    if (baseUrl != null) {
      return baseUrl;
    }
    else {
      return RestClient.getApplicationRoot();
    }
  }

  /**
   * Sets the base URL of the remote service and overrides the configured
   * default application root path.
   *
   * @param baseUrl
   *          the base URL used to contact the remote service
   */
  public void setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Returns the list of success codes used by this proxy.
   *
   * @return list of success codes, null if no custom success codes were
   *         provided.
   */
  public List<Integer> getSuccessCodes() {
    return successCodes;
  }

  /**
   * Sets a list of HTTP status codes that will be used to determine whether a
   * request was successful or not.
   *
   * @param successCodes
   *          list of HTTP status codes
   */
  public void setSuccessCodes(final List<Integer> successCodes) {
    this.successCodes = successCodes;
  }

  @SuppressWarnings({ "rawtypes" })
  protected void sendRequest(final RequestBuilder requestBuilder, final String body,
          final ResponseDemarshallingCallback demarshallingCallback) {
    sendRequest(requestBuilder, body, demarshallingCallback, true);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void sendRequest(final RequestBuilder requestBuilder, final String body,
          final ResponseDemarshallingCallback demarshallingCallback, final boolean firstTry) {

    final RemoteCallback remoteCallback = getRemoteCallback();
    try {
      // Allow overriding of request body in client-side interceptors
      final String requestBody = (requestBuilder.getRequestData() != null) ? requestBuilder.getRequestData() : body;
      if (ClientCSRFTokenCache.hasAssignedCSRFToken()) {
        requestBuilder.setHeader(Constants.ERRAI_CSRF_TOKEN_HEADER, ClientCSRFTokenCache.getAssignedCSRFToken());
      }
      final Request request = requestBuilder.sendRequest(requestBody, new RequestCallback() {
        @Override
        public void onError(final Request request, final Throwable throwable) {
          handleError(throwable, request, null);
        }

        @Override
        public void onResponseReceived(final Request request, final Response response) {
          final int statusCode = response.getStatusCode();
          if (firstTry && statusCode == 403 && response.getHeader(Constants.ERRAI_CSRF_TOKEN_HEADER) != null) {
            ClientCSRFTokenCache.setAssignedCSRFToken(response.getHeader(Constants.ERRAI_CSRF_TOKEN_HEADER));
            sendRequest(requestBuilder, requestBody, demarshallingCallback, false);
          }
          else if ((successCodes == null || successCodes.contains(statusCode)) && (statusCode >= 200 && statusCode < 300)) {
            final Object demarshalledValue;
            try {
              demarshalledValue = demarshallingCallback.demarshallResponse(response);
            } catch (final Throwable t) {
              throw new RuntimeException("An error occurred while demarshalling the body of the response. ", t);
            }
            remoteCallback.callback(demarshalledValue);
          }
          else {
            Throwable throwable = null;
            final ErrorCallback<?> errorCallback = getErrorCallback();
            if (hasExceptionMapper()) {
              throwable = unmarshallException(response);
            }
            else if (response.getText() != null && !response.getStatusText().equals("")) {
              throwable = new ResponseException(response.getStatusText(), response);
            }
            else {
              throwable = new ResponseException("Response returned with status=" + response.getStatusCode(), response);
            }
            handleError(throwable, request, response);
          }
        }
      });
      if (requestCallback != null) {
        requestCallback.callback(request);
      }
    } catch (final RequestException throwable) {
      handleError(throwable, null, null);
    }
  }

  /**
   * Uses the configured {@link ClientExceptionMapper} to unmarshal the
   * {@link Response} into a {@link Throwable}.
   *
   * @param response
   */
  protected Throwable unmarshallException(final Response response) {
    return getExceptionMapper().fromResponse(response);
  }

  protected void handleError(final Throwable throwable, final Request request, final Response response) {
    final ErrorCallback<?> errorCallback = getErrorCallback();
    if (errorCallback != null) {
      if (errorCallback instanceof RestErrorCallback) {
        ((RestErrorCallback) errorCallback).error(request, throwable);
      }
      else {
        /*
         * Because of type erasure, there's no way to know if an ErrorCallback that doesn't implement RestErrorCallback takes
         * a request as an argument. But in some cases we want to wrap callbacks (such as in Caller<T>) to further control when
         * they are executed, and this is done in code upstream of RestErrorCallback.
         *
         * Thus, here we simply try to invoke with a request, and fallback to passing null if we get a casting exception.
         */
        try {
          ((ErrorCallback<? super Request>) errorCallback).error(request, throwable);
        }
        catch (final ClassCastException e) {
          logger.debug("Class cast exception when invoking error callback with request object: {}", request);
          logger.debug("Invoking error callback again with null.");
          errorCallback.error(null, throwable);
        }
      }
    }
    else {
      GWT.log(throwable.getMessage(), throwable);
    }
  }

  @Override
  public void setQualifiers(final Annotation[] annos) {
    // do nothing (no use for qualifiers on injected JAX-RS proxies yet)
  }

  @Override
  public void setBatch(@SuppressWarnings("rawtypes") final RpcBatch batch) {
    throw new UnsupportedOperationException("Batching of remote calls is not supported in errai jax-rs!");
  }

  public void setRequestCallback(final RemoteCallback<Request> requestCallback) {
    this.requestCallback = requestCallback;
  }

  /**
   * @return true if this proxy has a configured exception mapper
   */
  public boolean hasExceptionMapper() {
    return getExceptionMapper() != null;
  }

  /**
   * @return the exceptionMapper
   */
  public ClientExceptionMapper getExceptionMapper() {
    return exceptionMapper;
  }

  /**
   * @param exceptionMapper
   *          the exceptionMapper to set
   */
  public void setExceptionMapper(final ClientExceptionMapper exceptionMapper) {
    this.exceptionMapper = exceptionMapper;
  }
}
