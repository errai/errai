package org.jboss.errai.ioc.client.container.async;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class BeanVote {
  private final Set<CreationalCallback<?>> dependencies = new HashSet<CreationalCallback<?>>();
  private final Runnable onFinishRunnable;

  public BeanVote(final Runnable onFinishRunnable) {
    this.onFinishRunnable = onFinishRunnable;
  }

  public void wait(final CreationalCallback<?> callbackInstance) {
    dependencies.add(callbackInstance);
  }

  public void finish(final CreationalCallback<?> callbackInstance) {
    if (dependencies.remove(callbackInstance)) {
      if (dependencies.isEmpty()) {
        onFinishRunnable.run();
      }
    }
  }
}
