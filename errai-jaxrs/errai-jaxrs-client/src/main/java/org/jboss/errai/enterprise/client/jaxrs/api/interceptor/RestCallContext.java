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

package org.jboss.errai.enterprise.client.jaxrs.api.interceptor;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;

import com.google.gwt.http.client.RequestBuilder;

/**
 * Represents the context of an intercepted JAX-RS (REST) remote call.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class RestCallContext extends RemoteCallContext {
  private RequestBuilder requestBuilder;

  /**
   * Provides access to the {@link RequestBuilder} used to construct and execute
   * the call to the REST endpoint.
   * <p>
   * Note that a call to {@link #setParameters(Object[])} will change this
   * context's {@link RequestBuilder} instance to reflect the parameter changes
   * (affecting the URL and HTTP headers).
   *
   * @return the {@link RequestBuilder} with the URL, HTTP method and HTTP
   *         headers set.
   */
  public RequestBuilder getRequestBuilder() {
    return requestBuilder;
  }

  /**
   * Changes the {@link RequestBuilder} instance used to execute the call to the
   * REST endpoint.
   *
   * @param requestBuilder
   *          the {@link RequestBuilder} instance to use when proceeding with
   *          the request. Must not be null.
   */
  public void setRequestBuilder(final RequestBuilder requestBuilder) {
    this.requestBuilder = Assert.notNull(requestBuilder);
  }
}
