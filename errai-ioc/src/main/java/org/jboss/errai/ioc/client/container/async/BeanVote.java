package org.jboss.errai.ioc.client.container.async;

import com.google.gwt.user.client.Timer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class BeanVote {
  private static final CreationalCallback<?> DUMMY = new CreationalCallback<Object>() {
    @Override
    public void callback(final Object beanInstance) {
    }
  };

  private final Set<CreationalCallback<?>> dependencies = new HashSet<CreationalCallback<?>>();
  private final Throwable trace = new Throwable();
  private final Runnable onFinishRunnable;
  private final Timer timeOut = new Timer() {
    @Override
    public void run() {
      trace.printStackTrace();
      throw new RuntimeException("time asynchronously loading bean!");
    }
  };

  public BeanVote(final Runnable onFinishRunnable) {
    this.onFinishRunnable = onFinishRunnable;
    dependencies.add(DUMMY);
    timeOut.schedule(2000);
  }

  public void wait(final CreationalCallback<?> callbackInstance) {
    dependencies.add(callbackInstance);
  }

  public void finish(final CreationalCallback<?> callbackInstance) {
    if (dependencies.remove(callbackInstance)) {
      _finish();
    }
  }

  public void finish() {
     dependencies.remove(DUMMY);
    _finish();
  }

  private void _finish() {
    if (dependencies.isEmpty()) {
      timeOut.cancel();
      onFinishRunnable.run();
    }
  }
}
