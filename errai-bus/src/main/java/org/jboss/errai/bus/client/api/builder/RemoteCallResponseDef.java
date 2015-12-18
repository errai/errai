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

import org.jboss.errai.common.client.api.RemoteCallback;

/**
 * This interface, <tt>RemoteCallResponseDef</tt> is a template for creating a remote call response. It ensures that
 * the response point is constructed properly
 */
public interface RemoteCallResponseDef {

  /**
   * Sets the callback response function, which is called after an endpoint is reached
   *
   * @param returnType - the return type of the callback function
   * @param callback   - the callback function
   * @return an instance of <tt>RemoteCallErrorDef</tt>
   */
  public <T> RemoteCallErrorDef respondTo(Class<T> returnType, RemoteCallback<T> callback);
}
