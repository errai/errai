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
