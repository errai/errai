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

package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.CallableFuture;
import org.jboss.errai.bus.server.api.CallableFutureFactory;
import org.jboss.errai.bus.client.tests.support.AsyncRPCService;
import org.jboss.errai.bus.server.annotations.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Mike Brock
 */
@Service
public class AsyncRpcServiceImpl implements AsyncRPCService {
  @Override
  public CallableFuture<String> doSomeTask() {
    final CallableFuture<String> future = CallableFutureFactory.get().createFuture();

    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(5000);
          future.setValue("foobar");
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }
    });
    executorService.shutdown();

    return future;
  }
}
