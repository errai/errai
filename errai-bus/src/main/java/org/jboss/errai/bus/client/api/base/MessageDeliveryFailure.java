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

/**
 * Thrown to indicate that delivery could not be completed for a message which was given to ErraiBus. 
 */
@SuppressWarnings("serial")
public class MessageDeliveryFailure extends RuntimeException {
  private boolean rpcEndpointException = false;
  
  public MessageDeliveryFailure() {
  }

  public MessageDeliveryFailure(final String message) {
    super(message);
  }

  public MessageDeliveryFailure(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  public MessageDeliveryFailure(final String message, final Throwable cause, boolean rpcEndpointException) {
    super(message, cause);
    this.rpcEndpointException = rpcEndpointException;
  }

  public MessageDeliveryFailure(final Throwable cause) {
    super(cause);
  }
  
  public boolean isRpcEndpointException() {
    return rpcEndpointException;
  }
}
