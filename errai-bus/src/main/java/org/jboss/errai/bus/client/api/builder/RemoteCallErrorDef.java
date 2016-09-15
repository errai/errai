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

package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.common.client.api.ErrorCallback;

/**
 * Template for creating a remote call error handler.
 */
public interface RemoteCallErrorDef {

  /**
   * Configures the error handler function
   *
   * @param errorCallback the error handler function, if null the default error handling will be used.
   * @return an instance of {@link RemoteCallSendable}
   */
  public RemoteCallSendable errorsHandledBy(ErrorCallback<?> errorCallback);

  /**
   * Activates the default error handler function. 
   *
   * @return an instance of {@link RemoteCallSendable}
   */
  public RemoteCallSendable defaultErrorHandling();
}
