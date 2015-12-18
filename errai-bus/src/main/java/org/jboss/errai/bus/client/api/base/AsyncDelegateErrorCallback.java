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

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.tasks.HasAsyncTaskRef;
import org.jboss.errai.bus.client.api.messaging.Message;

/**
 * An error callback decorator for repeating tasks which automatically cancels
 * them when they cause an error. Can also be used on its own, without wrapping
 * another error callback.
 */
public class AsyncDelegateErrorCallback implements ErrorCallback<Message> {
  private final HasAsyncTaskRef asyncTaskRef;
  private final ErrorCallback delegate;

  /**
   * Creates an error callback that optionally wraps another error callback. In
   * either case, future executions of the given task will be cancelled.
   *
   * @param task
   *          The task whose failures will be handled by this wrapper callback.
   *          Must not be null.
   * @param delegate
   *          The ErrorCallback that should be wrapped. Can be null, in which
   *          case errors in executions of {@code task} are logged to System.out.
   */
  public AsyncDelegateErrorCallback(final HasAsyncTaskRef task, final ErrorCallback delegate) {
    this.asyncTaskRef = Assert.notNull(task);
    this.delegate = delegate;
  }

  /**
   * Cancels future executions of the task by calling
   * {@link org.jboss.errai.common.client.api.tasks.AsyncTask#cancel(boolean)} on the AsyncTask that controls its execution.
   *
   * @return the value returned by the delegate error handler's
   *         <code>error()</code> method. If there is no delegate, the return
   *         value is always {@code true}.
   */
  @Override
  public boolean error(final Message message, final Throwable throwable) {
    if (asyncTaskRef.getAsyncTask() == null) {
      System.err.println("Unable to access async task reference! Cannot safely cancel task.");
    }
    else {
      asyncTaskRef.getAsyncTask().cancel(true);
    }

    if (delegate == null) {
      throwable.printStackTrace(System.out);
      return true;
    }
    else {
      return delegate.error(message, throwable);
    }
  }
}
