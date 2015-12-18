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

package org.jboss.errai.bus.server.api;

import org.jboss.errai.bus.client.api.CallableFuture;

/**
 * Used for obtaining instances of {@link org.jboss.errai.bus.client.api.CallableFuture} for use in asynchronous RPC methods.
 *
 * @author Mike Brock
 */
public class CallableFutureFactory {
  private static final CallableFutureFactory CALLABLE_FUTURE_FACTORY
      = new CallableFutureFactory();

  private CallableFutureFactory() {
  }

  public static CallableFutureFactory get() {
    return CALLABLE_FUTURE_FACTORY;
  }

  /**
   * Creates a new {@code CallableFuture} that can be returned for an asynchronous RPC method and used
   * for providing a value back to the client when a long-running process is done.
   *
   * @param <T> The type of value to be returned.
   * @return
   *          and instance of the {@link org.jboss.errai.bus.client.api.CallableFuture}.
   */
  public <T> CallableFuture<T> createFuture() {
    return new ServerCallableFuture<T>();
  }
}
