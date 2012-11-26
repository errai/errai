package org.jboss.errai.ioc.client.test;

import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.common.client.util.LogUtil;

/**
 * This class is designed to create a randomized delay in the callback to simulate network latency.
 *
 * @author Mike Brock
 */
public class FakeGWT {
  public static Throwable trace;

  public static void runAsync(final RunAsyncCallback callback) {
    final int delay = Random.nextInt(50) + 1;

    final Throwable _trace = new Throwable();
    new Timer() {
      @Override
      public void run() {
        trace = _trace;
        callback.onSuccess();
      }
    }.schedule(delay);

    LogUtil.log("simulating async load with " + delay + "ms delay.");
  }
}


