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

package org.jboss.errai.config.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Mike Brock
 */
public class ThreadUtil {
  private static final ExecutorService executorService
      = Executors.newCachedThreadPool();

  public static void execute(final Runnable runnable) {
    executorService.execute(runnable);
  }

  public static <T> Future<T> submit(final Callable<T> runnable) {
    return executorService.submit(runnable);
  }

  public static Future<?> submit(final Runnable runnable) {
    return executorService.submit(runnable);
  }

  public static void stopExecutor() {
    executorService.shutdown();
  }
}
