/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.framework.transports;

import com.google.gwt.http.client.Request;

import org.jboss.errai.bus.client.api.RetryInfo;
import org.jboss.errai.bus.client.api.TransportError;
import org.jboss.errai.common.client.api.Assert;

/**
* @author Mike Brock
*/
public final class BusTransportError implements TransportError {
  boolean stopDefaultErrorHandler = false;

  private final TransportHandler source;
  private final Request request;
  private final Throwable throwable;
  private final int statusCode;
  private final RetryInfo retryInfo;


  public BusTransportError(final TransportHandler source,
                           final Request request,
                           final Throwable throwable,
                           final int statusCode,
                           final RetryInfo retryInfo) {

    this.source = Assert.notNull(source);
    this.request = request;
    this.throwable = throwable;
    this.statusCode = statusCode;
    this.retryInfo = Assert.notNull(retryInfo);
  }

  @Override
  public TransportHandler getSource() {
    return source;
  }
  
  @Override
  public Request getRequest() {
    return request;
  }

  @Override
  public String getErrorMessage() {
    return throwable != null ? throwable.getMessage() : "";
  }

  @Override
  public boolean isHTTP() {
    return true;
  }

  @Override
  public boolean isWebSocket() {
    return false;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public Throwable getException() {
    return throwable;
  }

  @Override
  public void stopDefaultErrorHandling() {
    stopDefaultErrorHandler = true;
  }

  public boolean isStopDefaultErrorHandler() {
    return stopDefaultErrorHandler;
  }

  @Override
  public RetryInfo getRetryInfo() {
    return retryInfo;
  }
}
