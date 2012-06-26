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

package org.jboss.errai.enterprise.client.jaxrs.api.interceptor;

import org.jboss.errai.bus.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.framework.Assert;

import com.google.gwt.http.client.RequestBuilder;

/**
 * Represents the context of an intercepted JAX-RS (REST) remote call.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class RestCallContext extends RemoteCallContext {
  private RequestBuilder requestBuilder;
  
  /**
   * Provides access to the {@link RequestBuilder} used to construct and execute the call to the REST endpoint.
   * 
   * @return the {@link RequestBuilder} with the HTTP method, URL, and HTTP headers set.
   */
  public RequestBuilder getRequestBuilder() {
    return requestBuilder;
  }

  /**
   * Changes the {@link RequestBuilder} instance used to executed the call to the REST endpoint.
   * 
   * @param requestBuilder
   *          the {@link RequestBuilder} instance to use when proceeding with the request. Must not be null.
   */
  public void setRequestBuilder(RequestBuilder requestBuilder) {
    this.requestBuilder = Assert.notNull(requestBuilder);
  }
}
