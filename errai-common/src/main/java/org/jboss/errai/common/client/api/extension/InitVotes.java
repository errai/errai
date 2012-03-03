/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.common.client.api.extension;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.common.client.api.tasks.TaskManagerFactory;
import org.jboss.errai.common.client.util.TimeUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.jboss.errai.common.client.util.LogUtil.log;

/**
 * The <tt>InitVotes</tt> class provides the central algorithm around which disparate services within the Errai
 * Framework can elect to prevent initialization and be notified when initialization occurs. This is required
 * internally to ensure that services such as RPC proxies have been properly bound prior to any remote calls being
 * made. This API also makes it possible for user-defined services and extensions to Errai to participate in the
 * startup contract.
 *
 * @author Mike Brock
 */
public final class InitVotes {
  private InitVotes() {
  }

  private static final List<Runnable> initCallbacks = new ArrayList<Runnable>();
  private static final List<Runnable> oneTimeInitCallbacks = new ArrayList<Runnable>();

  private static boolean armed = false;
  private static final Set<String> waitForSet = new HashSet<String>();

  private static int timeoutMillis = !GWT.isProdMode() ? 60000 : 5000;

  private static volatile AsyncTask initTimeout;

  private static final Object lock = new Object();


  /**
   * Resets the state, clearing all current waiting votes and disarming the startup process. Calling <tt>reset()</tt>
   * does not however clear out any initialization callbacks registered with {@link #registerInitCallback(Runnable)}.
   */
  public static void reset() {
    synchronized (lock) {
      cancelFailTimer();
      oneTimeInitCallbacks.clear();
      waitForSet.clear();
      armed = false;
    }
  }

  /**
   * Specifies the number of milliseconds that will be permitted to transpire until dependencies are assumed
   * to have failed to satisfy, and thus an error is rendered to the browser console. The default value is
   * 5000 milliseconds.
   *
   * @param millis milliseconds.
   */
  public static void setTimeoutMillis(int millis) {
    timeoutMillis = millis;
  }

  /**
   * Declares a startup dependency on the specified class. By doing so, initialization of the framework services
   * will be blocked until a {@link #voteFor(Class)} is called with the same <tt>Class</tt> reference passed
   * to this method.
   * <p/>
   * If no dependencies have previously been declared, then the first caller to invoke this method arms and
   * begins the startup process. This starts the timer window (see {@link #setTimeoutMillis(int)}) for which
   * all components being waited on are expected to report back that they're ready.
   *
   * @param clazz a class reference.
   * @see #voteFor(Class)
   */
  public static void waitFor(final Class<?> clazz) {
    waitFor(clazz.getName());
  }

  private static void waitFor(String topic) {
    synchronized (lock) {
      if (waitForSet.contains(topic)) return;

      log("wait for: " + topic);

      if (!armed && waitForSet.isEmpty()) {
        beginInit();
      }

      waitForSet.add(topic);
    }
  }

  /**
   * Votes for initialization and removes a lock on the initialization of framework services. If the initialization
   * process has been armed and this vote releases the final dependency, the initialization process will be triggered,
   * calling all the registered initialization callbacks. See: {@link #registerInitCallback(Runnable)}
   *
   * @param clazz a class reference
   */
  public static void voteFor(final Class<?> clazz) {
    voteFor(clazz.getName());

  }

  private static void voteFor(String topic) {
    synchronized (lock) {
      log("vote For: " + topic);

      waitForSet.remove(topic);

      if (!waitForSet.isEmpty())
        log("  still waiting for -> " + waitForSet);

      if (armed && waitForSet.isEmpty()) {
        initWindow();
      }
    }
  }

  private static void initWindow() {
    synchronized (lock) {
      cancelFailTimer();
      TaskManagerFactory.get().schedule(TimeUnit.MILLISECONDS, 50, new Runnable() {
        @Override
        public void run() {
          cancelFailTimer();
          if (armed && waitForSet.isEmpty()) {
            finishInit();
          }
        }
      });
    }

  }

  /**
   * Registers a callback task to be executed once initialization occurs. Callbacks registered with this method
   * will be persistent <em>across</em> multiple initializations, and will not be cleared out even if {@link #reset()}
   * is called. If this is not desirable, see: {@link #registerOneTimeInitCallback};
   *
   * @param runnable a callback to execute
   */
  public static void registerInitCallback(final Runnable runnable) {
    synchronized (lock) {
      initCallbacks.add(runnable);
    }
  }

  /**
   * Registers a one-time callback task to be executed once initialization occurs. Unlike callbacks registered with
   * {@link #registerInitCallback(Runnable)} Callback(Runnable)}, callbacks registered with this method will only
   * be executed once and will never be used again if framework services are re-initialized.
   *
   * @param runnable a callback to execute
   */
  public static void registerOneTimeInitCallback(final Runnable runnable) {
    synchronized (lock) {
      oneTimeInitCallbacks.add(runnable);
    }
  }

  private static void beginInit() {
    synchronized (lock) {
      if (armed) {
        throw new RuntimeException("attempt to arm voting process more than once.");
      }

      armed = true;

      initTimeout = TaskManagerFactory.get().schedule(TimeUnit.MILLISECONDS, timeoutMillis, new Runnable() {
        @Override
        public void run() {
          synchronized (lock) {
            if (waitForSet.isEmpty() || !armed) return;

            log("components failed to initialize");
            for (String comp : waitForSet) {
              log("   [failed] -> " + comp);
            }
          }
        }
      });
    }
  }

  private static void finishInit() {
    synchronized (lock) {
      armed = false;
      cancelFailTimer();

      Iterator<Runnable> iter = oneTimeInitCallbacks.iterator();
      while (iter.hasNext()) {
        try {
          iter.next().run();
        }
        finally {
          iter.remove();
        }
      }

      for (Runnable callback : initCallbacks) {
        callback.run();
      }
    }
  }

  private static void cancelFailTimer() {
    synchronized (lock) {
      if (initTimeout != null && !initTimeout.isCancelled()) {
        initTimeout.cancel(true);
      }
    }
  }
}
