package org.jboss.errai.ioc.client.container.async;

import com.google.gwt.user.client.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class AsyncBeanContext {
  private static final CreationalCallback<?> DUMMY = new CreationalCallback<Object>() {
    @Override
    public void callback(final Object beanInstance) {
    }
  };

  private enum FireState {
    NOT_FIRED, ARMED, FIRED
  }

  /**
   * These are dependencies immediately needed for construction of the bean. But for mixed injection, may not
   * be all the required dependencies.
   */
  private final Set<CreationalCallback<?>> constructDependencies = new LinkedHashSet<CreationalCallback<?>>();

  /**
   * These are all of the bean dependencies for both field and parameter-based injection.
   */
  private final Set<CreationalCallback<?>> allDependencies = new LinkedHashSet<CreationalCallback<?>>();

  /**
   * This is a holder of all the values associated with each callback.
   */
  private final Map<CreationalCallback<?>, Object> valueMap = new HashMap<CreationalCallback<?>, Object>();

  /**
   * For debug tracing.
   */
  private final Throwable trace = new Throwable();

  private Runnable onConstructRunnable;
  private FireState constructFireState = FireState.NOT_FIRED;


  private final List<Runnable> onFinishRunnables = new ArrayList<Runnable>();
  private FireState finishFireState = FireState.NOT_FIRED;

  private Object constructedObject;

  private final Timer timeOut = new Timer() {
    @Override
    public void run() {
      trace.printStackTrace();

      for (final CreationalCallback callback : allDependencies) {
        System.out.println(" --unsatisfied-> " + callback.toString());
      }
    }
  };

  public AsyncBeanContext() {
    allDependencies.add(DUMMY);
    timeOut.schedule(2000);
  }

  public void setConstructedObject(final Object constructedObject) {
    this.constructedObject = constructedObject;
  }

  public void setOnConstruct(final Runnable runnable) {
    this.onConstructRunnable = runnable;
    _constructCheck();
  }

  public void addOnFinish(final Runnable runnable) {
    onFinishRunnables.add(runnable);
    _finishCheck();
  }

  public void waitConstruct(final CreationalCallback<?> callbackInstance) {
    constructDependencies.add(callbackInstance);
    wait(callbackInstance);
  }

  public void wait(final CreationalCallback<?> callbackInstance) {
    allDependencies.add(callbackInstance);
  }

  public Object getConstructedObject() {
    return constructedObject;
  }

  public void finish(final CreationalCallback<?> callbackInstance) {
    finish(callbackInstance, null);
  }

  public void finish(final CreationalCallback<?> callbackInstance, final Object beanValue) {
    if (allDependencies.remove(callbackInstance)) {
      valueMap.put(callbackInstance, beanValue);

      if (constructDependencies.remove(callbackInstance)) {
        _constructCheck();
      }

      _finishCheck();
    }
  }

  public Object getBeanValue(final CreationalCallback<?> callbackInstance) {
    return valueMap.get(callbackInstance);
  }

  public void finish() {
    allDependencies.remove(DUMMY);
    _finishCheck();
  }

  private void _constructCheck() {
    if (constructFireState == FireState.FIRED) {
      return;
    }

    if (constructDependencies.isEmpty()) {
      constructFireState = FireState.ARMED;
      if (onConstructRunnable != null) {
        onConstructRunnable.run();
        constructFireState = FireState.FIRED;
      }
    }
  }

  private void _finishCheck() {
    if (finishFireState == FireState.FIRED) {
      return;
    }

    if (allDependencies.isEmpty()) {
      timeOut.cancel();
      constructFireState = FireState.ARMED;

      if (!onFinishRunnables.isEmpty()) {
        for (final Runnable runnable : onFinishRunnables) {
          runnable.run();
        }

        constructFireState = FireState.FIRED;
      }
    }
  }
}
