/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.support.bus.tests.client.res;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.UncaughtException;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ioc.client.api.EntryPoint;

@EntryPoint
public class ExceptionHandlingBean {

  public interface VerificationCallback {
    public void callback(Throwable t1, Throwable t2);
  }

  private Throwable throwable1;
  private Throwable throwable2;
  private VerificationCallback callback;

  @Inject
  private Caller<ExceptionService> exceptionServiceCaller;

  @UncaughtException
  private void onException1(Throwable t) {
    throwable1 = t;
    if (throwable2 != null && callback != null) {
      callback.callback(throwable1, throwable2);
    }
  }

  @UncaughtException
  public void onException2(Throwable t) {
    throwable2 = t;
    if (throwable1 != null && callback != null) {
      callback.callback(throwable1, throwable2);
    }
  }

  public void setVerificationCallback(VerificationCallback callback) {
    this.callback = callback;
  }

  public Caller<ExceptionService> getCaller() {
    return exceptionServiceCaller;
  }

}
