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
 * A <tt>CallableFuture</tt> is something that can be used to reply to an RPC call asynchronously. Particularly for
 * long running processes. RPC methods which return this type will be treated automatically as asynchronous.
 * <p>
 * The RPC reply only occurs when the {@link #setValue(Object)} method is invoked.
 *
 * @author Mike Brock
 */
public interface CallableFuture<T> {

  /**
   * The method called to provide the result value into the future. Calling this method immediately dispatches
   * a response to the caller, completing the asynchronous call.
   *
   * @param responseValue
   *    the response value of the call.
   */
  public void setValue(T responseValue);
}
