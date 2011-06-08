/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.HasAsyncTaskRef;
import org.jboss.errai.bus.client.api.Message;

public class AsyncDelegateErrorCallback implements ErrorCallback {
  private HasAsyncTaskRef asyncTaskRef;
  private ErrorCallback delegate;

  public AsyncDelegateErrorCallback(HasAsyncTaskRef ref, ErrorCallback delegate) {
    this.asyncTaskRef = ref;
    this.delegate = delegate;
  }

  public boolean error(Message message, Throwable throwable) {
    if (asyncTaskRef.getAsyncTask() == null) {
      System.err.println("Unable to access async task reference! Cannot safely cancel task.");
    }
    else {
      asyncTaskRef.getAsyncTask().cancel(true);
    }

    if (delegate == null) {
      throwable.printStackTrace();
      return true;
    }
    else {
      return delegate.error(message, throwable);
    }
  }
}
