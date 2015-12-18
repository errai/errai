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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.Key;

/**
 * Thrown when an attempt at sending or receiving a message results in an error on the network level (e.g. an HTTP
 * response code of 4xx or 5xx).
 */
@SuppressWarnings("serial")
public class TransportIOException extends Exception {
  private final int errorCode;
  private final String errorMessage;

  public TransportIOException(@MapsTo("message") String message, @MapsTo("errorCode") int errorCode,
      @MapsTo("errorMessage") String errorMessage) {
    super(message);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  @Key("errorCode")
  public int errorCode() {
    return errorCode;
  }

  @Key("errorMessage")
  public String getErrorMessage() {
    return errorMessage;
  }
}
