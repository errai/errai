/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.api.interceptor;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;

/**
 * Represents the context of a call to an intercepted remote call.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class RemoteCallContext extends CallContext {

  /**
   * Proceeds with the execution of the intercepted remote calls.
   * 
   * @param callback
   *          The remote callback that receives the return value from the call. This callback is guaranteed to be
   *          invoked before the callback provided on the actual call site. Cannot be null.
   */
  public abstract void proceed(RemoteCallback<?> callback);

  /**
   * Proceeds with the execution of the intercepted method.
   * 
   * @param callback
   *          The remote callback that receives the return value from the call. This callback is guaranteed to be
   *          invoked before the callback provided on the actual call site. Cannot be null.
   * 
   * @param errorCallback
   *          The error callback that receives transmission errors and exceptions thrown by the remote service. This
   *          error callback is guaranteed to be invoked before the error callback provided on the actual call site.
   *          Cannot be null.
   */
  public abstract void proceed(RemoteCallback<?> callback, ErrorCallback errorCallback);
}
