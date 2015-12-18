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

package org.jboss.errai.ioc.support.bus.client;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.builder.RemoteCallSendable;
import org.jboss.errai.common.client.api.BatchCaller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.common.client.framework.RpcBatch;
import org.jboss.errai.common.client.framework.RpcStub;
import org.jboss.errai.ioc.client.api.IOCProvider;

/**
 * {@link IOCProvider} to make {@link BatchCaller} instances injectable.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@IOCProvider
@Singleton
@SuppressWarnings({"rawtypes", "unchecked"}) 
public class BatchCallerProvider implements Provider<BatchCaller> {
  private static final RemoteServiceProxyFactory factory = new RemoteServiceProxyFactory();

  @Override
  public BatchCaller get() {
    return new BatchCaller() {
      private RpcBatchImpl batch = new RpcBatchImpl();
      
      @Override
      public <T> T call(RemoteCallback<?> callback, Class<T> remoteService) {
        return call(callback, null, remoteService);
      }

      @Override
      public <T> T call(final RemoteCallback<?> callback, final ErrorCallback<?> errorCallback, final Class<T> remoteService) {
        final T proxy = factory.getRemoteProxy(remoteService);
        ((RpcStub) proxy).setRemoteCallback(new BatchRemoteCallback(batch, callback));
        ((RpcStub) proxy).setErrorCallback(new BatchErrorCallback(batch, errorCallback));
        ((RpcStub) proxy).setBatch(batch);
        return proxy;
      }

      @Override
      public void sendBatch() {
        batch.flush();
        batch = new RpcBatchImpl();
      }

      @Override
      public void sendBatch(RemoteCallback<Void> callback) {
        batch.successCallback = callback;
        sendBatch();
      }

      @Override
      public void sendBatch(ErrorCallback<?> errorCallback) {
        batch.errorCallback = errorCallback;
        sendBatch();
      }

      @Override
      public void sendBatch(RemoteCallback<Void> callback, ErrorCallback<?> errorCallback) {
        batch.successCallback = callback;
        batch.errorCallback = errorCallback;
        sendBatch();
      }
    };
  }

  private class RpcBatchImpl implements RpcBatch<RemoteCallSendable> {
    private final List<RemoteCallSendable> queuedRequests = new ArrayList<RemoteCallSendable>();
    private final List<RemoteCallback<?>> pendingCallbacks = new ArrayList<RemoteCallback<?>>();
    private RemoteCallback<Void> successCallback;
    private ErrorCallback errorCallback;

    @Override
    public void addRequest(RemoteCallSendable request) {
      queuedRequests.add(request);
    }

    @Override
    public void flush() {
      for (RemoteCallSendable request : queuedRequests) {
        request.sendNowWith(ErraiBus.get());
      }
      queuedRequests.clear();
    }
    
    public void onSuccess(RemoteCallback<?> callback) {
      pendingCallbacks.remove(callback);
      if (pendingCallbacks.isEmpty() && successCallback != null) {
        successCallback.callback(null);
      }
    }
    
    public void onError(Message m, Throwable t) {
      if (errorCallback != null) {
        errorCallback.error(m, t);
      }
    }
  }
  
  private class BatchRemoteCallback<R> implements RemoteCallback<R> {
    private final RpcBatchImpl batch;
    private final RemoteCallback<R> remoteCallback;
    
    public BatchRemoteCallback(RpcBatchImpl batch, RemoteCallback<R> remoteCallback) {
      this.batch = batch;
      this.remoteCallback = remoteCallback;
      this.batch.pendingCallbacks.add(remoteCallback);
    }
    
    @Override
    public void callback(R response) {
      remoteCallback.callback(response);
      batch.onSuccess(remoteCallback);
    }
  }

  private class BatchErrorCallback extends BusErrorCallback {
    private final RpcBatchImpl batch;
    private final ErrorCallback errorCallback;
    
    public BatchErrorCallback(RpcBatchImpl batch, ErrorCallback errorCallback) {
      this.batch = batch;
      this.errorCallback = errorCallback;
    }
    
    @Override
    public boolean error(Message message, Throwable throwable) {
      if (errorCallback != null) {
        errorCallback.error(message, throwable);
      }
      else {
        DefaultErrorCallback.INSTANCE.error(message, throwable);
      }
      
      batch.onError(message, throwable);
      return false;
    }
  }
}
