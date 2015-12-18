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

package org.jboss.errai.ioc.support.bus.tests.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.BatchCaller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.support.bus.client.BatchCallerProvider;
import org.jboss.errai.ioc.support.bus.tests.client.res.RpcBatchService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

/**
 * Tests RPC batching.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BatchCallerIntegrationTest extends AbstractErraiIOCBusTest {

  private class TestBatchCaller implements BatchCaller {

    private final BatchCaller caller;
    private boolean flushed;

    public TestBatchCaller(BatchCaller caller) {
      this.caller = caller;
    }

    @Override
    public <T> T call(RemoteCallback<?> callback, Class<T> remoteService) {
      return caller.call(callback, remoteService);
    }

    @Override
    public <T> T call(RemoteCallback<?> callback, ErrorCallback<?> errorCallback, Class<T> remoteService) {
      return caller.call(callback, errorCallback, remoteService);
    }

    @Override
    public void sendBatch() {
      caller.sendBatch();
      flushed = true;
    }

    @Override
    public void sendBatch(RemoteCallback<Void> callback) {
      caller.sendBatch(callback);
      flushed = true;
    }

    @Override
    public void sendBatch(ErrorCallback<?> errorCallback) {
      caller.sendBatch(errorCallback);
      flushed = true;
    }

    @Override
    public void sendBatch(RemoteCallback<Void> callback, ErrorCallback<?> errorCallback) {
      caller.sendBatch(callback, errorCallback);
      flushed = true;
    }
  }

  public void testBatchedRpc() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<String> methodsCalled = new ArrayList<String>();
        final TestBatchCaller batchCaller = new TestBatchCaller(new BatchCallerProvider().get());

        batchCaller.call(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertTrue(batchCaller.flushed);
            methodsCalled.add(response);
          }
        }, RpcBatchService.class).batchedMethod1();

        batchCaller.call(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertTrue(batchCaller.flushed);
            methodsCalled.add(response);
            assertEquals(Arrays.asList("batchedMethod1", "batchedMethod2"), methodsCalled);
            finishTest();
          }
        }, RpcBatchService.class).batchedMethod2();

        batchCaller.sendBatch();
      }
    });
  }

  public void testBatchedRpcWithSuccessCallback() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final List<String> methodsCalled = new ArrayList<String>();
        final TestBatchCaller batchCaller = new TestBatchCaller(new BatchCallerProvider().get());

        batchCaller.call(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertTrue(batchCaller.flushed);
            methodsCalled.add(response);
          }
        }, RpcBatchService.class).batchedMethod1();

        batchCaller.call(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertTrue(batchCaller.flushed);
            methodsCalled.add(response);
            assertEquals(Arrays.asList("batchedMethod1", "batchedMethod2"), methodsCalled);
          }
        }, RpcBatchService.class).batchedMethod2();

        batchCaller.sendBatch(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            assertTrue(batchCaller.flushed);
            assertEquals(Arrays.asList("batchedMethod1", "batchedMethod2"), methodsCalled);
            finishTest();
          }

        });
      }
    });
  }

  public void testBatchedRpcWithErrorCallback() {
    final UncaughtExceptionHandler oldHandler = GWT.getUncaughtExceptionHandler();
    GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void onUncaughtException(Throwable t) {
        if (!(t.getMessage().contains("batchedMethodThrowsException"))) { 
          // only let the test fail in case we get an exception we didn't anticipate
          oldHandler.onUncaughtException(t);
        }
      }
    });

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final TestBatchCaller batchCaller = new TestBatchCaller(new BatchCallerProvider().get());

        batchCaller.call(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            fail("ErrorCallback should have been invoked");
          }
        }, RpcBatchService.class).batchedMethodThrowsException();

        batchCaller.sendBatch(new BusErrorCallback() {
          @Override
          public boolean error(Message message, Throwable throwable) {
            assertTrue(batchCaller.flushed);
            finishTest();
            return false;
          }

        });
      }
    });
  }
}
