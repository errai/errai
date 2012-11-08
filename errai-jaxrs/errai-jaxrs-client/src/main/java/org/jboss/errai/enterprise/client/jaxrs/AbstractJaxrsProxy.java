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

package org.jboss.errai.enterprise.client.jaxrs;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.framework.RpcStub;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * JAX-RS proxies are {@link RpcStub}s managed by the shared {@see RemoteServiceProxyFactory}. The implementations of
 * this class are generated.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractJaxrsProxy implements RpcStub {
  private String baseUrl;
  private List<Integer> successCodes;

  /**
   * Returns the remote callback used by this proxy.
   * 
   * @return the remote callback, never null.
   */
  @SuppressWarnings("rawtypes")
  public abstract RemoteCallback getRemoteCallback();

  /**
   * Returns the error callback used by this proxy.
   * 
   * @return the error callback, null if no error callback was provided.
   */
  public abstract ErrorCallback getErrorCallback();

  /**
   * If not set explicitly, the base url is the configured default application root path {@see RestClient}.
   * 
   * @return the base url used to contact the remote service
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
   * Sets the base url of the remote service and overrides the configured default application root path.
   * 
   * @param baseUrl
   *          the base url used to contact the remote service
   */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Returns the list of success codes used by this proxies.
   * 
   * @return list of success codes, null if no custom success codes were provided.
   */
  public List<Integer> getSuccessCodes() {
    return successCodes;
  }

  /**
   * Sets a list of HTTP status codes that will be used to determine whether a request was successful or not.
   * 
   * @param codes
   *          list of HTTP status codes
   */
  public void setSuccessCodes(List<Integer> successCodes) {
    this.successCodes = successCodes;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void sendRequest(final RequestBuilder requestBuilder, final String body,
      final ResponseDemarshallingCallback demarshallingCallback) {

    final RemoteCallback remoteCallback = getRemoteCallback();
    try {
      requestBuilder.sendRequest(body, new RequestCallback() {
        @Override
        public void onError(Request request, Throwable throwable) {
          handleError(throwable, null);
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (((successCodes == null) || successCodes.contains(response.getStatusCode()))
              && ((response.getStatusCode() >= 200) && (response.getStatusCode() < 300))) {
            if (remoteCallback instanceof ResponseCallback) {
              ((ResponseCallback) getRemoteCallback()).callback(response);
            }
            else {
              if (response.getStatusCode() == 204) {
                remoteCallback.callback(null);
              }
              else {
                remoteCallback.callback(demarshallingCallback.demarshallResponse(response.getText()));
              }
            }
          }
          else {
            ResponseException throwable = new ResponseException(response.getStatusText(), response);
            handleError(throwable, response);
          }
        }
      });
    }
    catch (RequestException throwable) {
      handleError(throwable, null);
    }
  }

  protected void handleError(Throwable throwable, Response response) {
    if (getErrorCallback() != null) {
      getErrorCallback().error(null, throwable);
    }
    else if ((getRemoteCallback() instanceof ResponseCallback) && (response != null)) {
      ((ResponseCallback) getRemoteCallback()).callback(response);
    }
    else {
      GWT.log(throwable.getMessage(), throwable);
    }
  }

  @Override
  public void setQualifiers(Annotation[] annos) {
    // do nothing (no use for qualifiers on injected JAX-RS proxies yet)
  }
}
