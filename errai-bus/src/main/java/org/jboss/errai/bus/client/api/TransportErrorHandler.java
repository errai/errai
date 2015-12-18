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

/**
 * A transport error handler is used for handling errors which arise from network communication problems between the
 * client bus and the remote server bus.
 *
 * @author Mike Brock
 */
public interface TransportErrorHandler {
  /**
   * Called by the bus upon an error that occurs during communication.
   *
   * @param error an {@link TransportError} containing details of the error and also error handling functionality.
   */
  public void onError(TransportError error);
}
