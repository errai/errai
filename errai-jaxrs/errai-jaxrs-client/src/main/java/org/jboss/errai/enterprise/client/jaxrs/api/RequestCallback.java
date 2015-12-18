/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;

/**
 * A {@link org.jboss.errai.common.client.api.RemoteCallback} that can be used
 * to retrieve the underlying HTTP request.
 */
public interface RequestCallback extends RemoteCallback<Request> {

  /**
   * Invoked by the JaxrsProxy after HTTP request has been initiated. If the
   * initiation of the request fails this method will not be invoked. Instead, a 
   * {@link RequestException} will be passed to the provided {@link ErrorCallback}.
   * 
   * @param request
   *          the {@link Request} that has been initiated.
   */
  void callback(Request request);
}
