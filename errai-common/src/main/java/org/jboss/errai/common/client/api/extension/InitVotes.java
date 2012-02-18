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

import com.google.gwt.user.client.Timer;
import org.jboss.errai.common.client.util.LogUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.errai.common.client.util.LogUtil.log;

/**
 * @author Mike Brock
 */
public final class InitVotes {
  private InitVotes() {
  }

  private static final List<Runnable> initCallbacks = new ArrayList<Runnable>();
  private static boolean armed = false;
  private static final Set<String> waitForSet = new HashSet<String>();
  private static Timer initTimeout = new Timer() {
    @Override
    public void run() {
      log("components failed to initialize");
      for (String comp : waitForSet) {
        log("   [failed] -> " + comp);
      }
    }
  };

  public static void reset() {
    waitForSet.clear();
    armed = false;
  }

  public static void waitFor(Class<?> clazz) {
    log("Wait For: " + clazz.getName());
    waitFor(clazz.getName());
  }

  private static void waitFor(String topic) {
    if (!armed && waitForSet.isEmpty()) {
      beginInit();
    }
    waitForSet.add(topic);
  }

  public static void voteFor(Class<?> clazz) {
    log("Vote For: " + clazz.getName());
    voteFor(clazz.getName());
    for (String waitFor : waitForSet) {
      log("  Still Waiting For -> " + waitFor);
    }
  }

  private static void voteFor(String topic) {
    waitForSet.remove(topic);

    if (armed && waitForSet.isEmpty()) {
      finishInit();
    }
  }

  public static void registerInitCallback(Runnable runnable) {
    initCallbacks.add(runnable);
  }

  private static void beginInit() {
    armed = true;
    initTimeout.schedule(5000);
  }

  private static void finishInit() {
    armed = false;
    initTimeout.cancel();
    for (Runnable callback : initCallbacks) {
      callback.run();
    }
  }
}
