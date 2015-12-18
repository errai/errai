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

import com.google.gwt.http.client.Request;

/**
 * Default implementation of {@link RequestCallback} that allows the
 * cancellation of pending {@link Request}s.
 */
public class RequestHolder implements RequestCallback {

  private Request request;

  @Override
  public void callback(Request request) {
    this.request = request;
  }

  /**
   * @return the {@link Request} or null if request has failed.
   */
  public Request getRequest() {
    return request;
  }

  /**
   * @return true if request has been initiated and is waiting for a response.
   */
  public boolean isAlive() {
    return request != null && request.isPending();
  }
}
