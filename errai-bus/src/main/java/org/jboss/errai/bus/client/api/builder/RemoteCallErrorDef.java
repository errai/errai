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
 * This interface, <tt>RemoteCallErrorDef</tt> is a template for creating a remote call error handler. It ensures that
 * the error is constructed properly
 */
public interface RemoteCallErrorDef {

  /**
   * Sets the error handler function and returns an instance of <tt>RemoteCallSendable</tt>
   *
   * @param errorCallback - the error handler
   * @return an instance of <tt>RemoteCallSendable</tt>
   */
  public RemoteCallSendable errorsHandledBy(ErrorCallback errorCallback);

  /**
   * Sets the default error handler function and returns an instance of <tt>RemoteCallSendable</tt>
   *
   * @return an instance of <tt>RemoteCallSendable</tt>
   */
  public RemoteCallSendable defaultErrorHandling();
}
