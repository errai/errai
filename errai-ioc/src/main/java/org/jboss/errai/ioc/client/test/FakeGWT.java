package org.jboss.errai.ioc.client.test;

import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;

/**
 * This class is designed to create a randomized delay in the callback to simulate network latency.
 *
 * @author Mike Brock
 */
public class FakeGWT {
  public static Throwable trace;

  public static void runAsync(final RunAsyncCallback callback) {
    final Throwable _trace = new Throwable();
    new Timer() {
      @Override
      public void run() {
        trace = _trace;
        callback.onSuccess();
      }
    }.schedule(Random.nextInt(150) + 1);
  }
}


