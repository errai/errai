/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.api;

import org.jboss.errai.bus.client.framework.transports.TransportHandler;

import com.google.gwt.http.client.Request;

/**
 * A class representing the details of a network/transport error on the bus.
 *
 * @author Mike Brock
 */
// TODO move this to the api package and make this a class in Errai 3.0
public interface TransportError {

  /**
   * The {@link TransportHandler} where this error came from. Never null.
   */
  public TransportHandler getSource();
  
  /**
   * The {@link Request} associated with the error.
   *
   * @return
   */
  public Request getRequest();

  /**
   * An error message associated with the error, if applicable. Otherwise an
   * empty string is returned.
   *
   * @return
   */
  public String getErrorMessage();

  /**
   * Returns true if the error occurred as the result of an HTTP request.
   *
   * @return true if HTTP
   */
  public boolean isHTTP();

  /**
   * Returns true if the error occurred as a result of a problem with a
   * WebSockets channel.
   *
   * @return true if WebSockets
   */
  public boolean isWebSocket();

  /**
   * Any applicable HTTP status code with the error. Otherwise returns -1.
   *
   * @return an HTTP status code.
   */
  public int getStatusCode();

  /**
   * Any exception associated with the error. Returns null if there's no
   * relevant exception.
   *
   * @return
   */
  public Throwable getException();

  /**
   * Causes the ClientMessageBus to behave as if this error did not happen.
   * Suppressing default error handling has the following implications:
   * <ul>
   * <li>No error dialogs or logging will appear
   * <li>No state transitions in the {@link BusLifecycleListener bus lifecycle}
   * will occur.
   * <li>The bus will not retry the failed communication attempt. This may cause
   * the bus to lose some or all of its ability to communicate with the server
   * (server-to-client messaging is especially vulnerable)
   * </ul>
   * <p>
   * To recover from this indeterminate state, restart the bus by calling
   * {@link ClientMessageBus#stop(boolean)} followed by
   * {@link ClientMessageBus#init()} at a later time.
   */
  public void stopDefaultErrorHandling();

  /**
   * Provides the retry information for the failed action that led to this error.
   *
   * @return A RetryInfo object. Never null.
   */
  public RetryInfo getRetryInfo();
}
