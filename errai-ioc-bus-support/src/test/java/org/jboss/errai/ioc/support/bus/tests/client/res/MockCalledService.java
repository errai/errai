/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
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

import java.lang.annotation.Annotation;

import org.jboss.errai.bus.client.api.builder.RemoteCallSendable;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.framework.RpcBatch;
import org.jboss.errai.common.client.framework.RpcStub;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@SuppressWarnings("rawtypes")
public class MockCalledService implements CalledService, RpcStub {

  public static enum Mode {
    REMOTE,
    ERROR,
    BATCH_REMOTE,
    BATCH_ERROR
  }

  private Mode mode = Mode.REMOTE;
  private RemoteCallback remoteCallback;
  private ErrorCallback errorCallback;
  private RpcBatch batch;

  public void setMode(final Mode mode) {
    this.mode = mode;
  }

  @SuppressWarnings("unchecked")
  @Override
  public String method() {
    switch (this.mode) {
    case REMOTE:
      remoteCallback.callback(null);
      return null;
    case ERROR:
      errorCallback.error(null, null);
      return null;
    case BATCH_REMOTE:
      batch.addRequest((RemoteCallSendable) bus -> remoteCallback.callback(null));
      return null;
    case BATCH_ERROR:
      batch.addRequest((RemoteCallSendable) bus -> errorCallback.error(null, null));
      return null;
    default:
      throw new IllegalArgumentException("Have not yet mocked out " + mode + " mode.");
    }
  }

  @Override
  public void setRemoteCallback(final RemoteCallback callback) {
    this.remoteCallback = callback;
  }

  @Override
  public void setErrorCallback(final ErrorCallback callback) {
    this.errorCallback = callback;
  }

  @Override
  public void setQualifiers(final Annotation[] annotations) {
  }

  @Override
  public void setBatch(final RpcBatch batch) {
    this.batch = batch;
  }

}
