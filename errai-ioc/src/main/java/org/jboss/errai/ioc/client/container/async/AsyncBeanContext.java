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

package org.jboss.errai.ioc.client.container.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

/**
 * @author Mike Brock
 */
public class AsyncBeanContext {
  private String comment;
  private Logger logger = LoggerFactory.getLogger(getClass());

  private static final CreationalCallback<?> DUMMY = new CreationalCallback<Object>() {
    @Override
    public void callback(final Object beanInstance) {
    }

    @Override
    public String toString() {
      return "INIT_PLACEHOLDER";
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

  private Runnable onConstructRunnable;
  private FireState constructFireState = FireState.NOT_FIRED;

  private final List<Runnable> onFinishRunnables = new ArrayList<Runnable>();
  private FireState finishFireState = FireState.NOT_FIRED;

  private Object constructedObject;

  private final Timer timeOut = new Timer() {
    @Override
    public void run() {
      timeOut.cancel();
      logger.warn("FAILED FOR: " + comment);

      logger.warn("unsatisfied dependencies:");
      for (final CreationalCallback callback : allDependencies) {
        logger.warn(" --unsatisfied-> " + callback.toString());
      }
    }
  };

  public AsyncBeanContext() {
    allDependencies.add(DUMMY);
    timeOut.schedule(10000);
  }

  public void setConstructedObject(final Object constructedObject) {
    this.constructedObject = constructedObject;
  }

  public void setOnConstruct(final Runnable runnable) {
    this.onConstructRunnable = runnable;
    _constructCheck();
  }

  public void runOnFinish(final Runnable runnable) {
    if (finishFireState == FireState.FIRED) {
      runnable.run();
    }
    else {
      onFinishRunnables.add(runnable);
      _finishCheck();
    }
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
      logger.warn("finish did not fire because state is already FIRED");
      return;
    }
    if (allDependencies.isEmpty()) {
      timeOut.cancel();
      finishFireState = FireState.ARMED;

      if (!onFinishRunnables.isEmpty()) {
        finishFireState = FireState.FIRED;

        final Iterator<Runnable> runnableIterable = onFinishRunnables.iterator();
        while (runnableIterable.hasNext()) {
          try {
            runnableIterable.next().run();
          }
          catch (Throwable t) {
            if (GWT.isProdMode() || !(t instanceof AssertionError)) {
              t.printStackTrace();
            }
            else {
              /*
               * Don't swallow assertion errors when running tests.
               */
              throw (AssertionError) t;
            }
          }
          runnableIterable.remove();
        }
      }
    }

  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
