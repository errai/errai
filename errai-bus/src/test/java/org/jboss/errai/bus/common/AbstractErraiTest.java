/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.common;

import static java.lang.Math.abs;
import static org.jboss.errai.bus.common.FloatUtil.DEFAULT_PRECISION;
import static org.jboss.errai.bus.common.FloatUtil.maxSignifigantDigitIndex;
import static org.jboss.errai.bus.common.FloatUtil.mostSignifigantDigitIndex;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.api.tasks.ClientTaskManager;
import org.jboss.errai.common.client.api.tasks.TaskManager;
import org.jboss.errai.common.client.api.tasks.TaskManagerFactory;
import org.jboss.errai.common.client.api.tasks.TaskManagerProvider;
import org.jboss.errai.common.client.logging.LoggingHandlerConfigurator;
import org.jboss.errai.common.client.logging.handlers.ErraiSystemLogHandler;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Base test class for testing ErraiBus-based code. Located in the main distribution so it can be extended
 * by other modules.
 */
public abstract class AbstractErraiTest extends GWTTestCase {
  protected static ClientMessageBus bus;
  protected static final void assertFloatArrayEquals(final float[] a, final float[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertApproximatelyEqual(a[i], b[i]);
      }
    }
  }

  protected static final void assertFloatArrayEquals(final Float[] a, final Float[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertApproximatelyEqual(a[i], b[i]);
      }
    }
  }

  protected static final void assertDoubleArrayEquals(final double[] a, final double[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertApproximatelyEqual(a[i], b[i]);
      }
    }
  }

  protected static final void assertDoubleArrayEquals(final Double[] a, final Double[] b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.length != b.length) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.length; i++) {
        assertApproximatelyEqual(a[i], b[i]);
      }
    }
  }

  protected static final void assertFloatListEquals(final List<Float> a, final List<Float> b) {
    if (a == null || b == null) {
      assertTrue(failMessage(a, b), a == null && b == null);
    }
    else if (a.size() != b.size()) {
      fail(failMessage(a, b));
    }
    else {
      for (int i = 0; i < a.size(); i++) {
        assertApproximatelyEqual(a.get(i), b.get(i));
      }
    }
  }

  protected static final <K> void assertFloatMapEquals(final Map<K, Float> a, final Map<K, Float> b) {
    if (a == null || b == null) {
      assertSame(a, b);
      return;
    }
  
    assertEquals(a.size(), b.size());
    for (final Entry<K, Float> entry : a.entrySet()) {
      assertTrue(b.containsKey(entry.getKey()));
      final Float aVal = entry.getValue();
      final Float bVal = b.get(entry.getKey());
      if (aVal == null || bVal == null) {
        assertSame(aVal, bVal);
      }
      else {
        assertApproximatelyEqual(aVal, bVal);
      }
    }
  }

  protected static void assertApproximatelyEqual(final Float a, final Float b) {
    if (a == null || b == null) {
      assertSame(a, b);
    }
    else {
      assertApproximatelyEqual(a.floatValue(), b.floatValue());
    }
  }

  protected static void assertApproximatelyEqual(final float a, final float b) {
    if (Float.isNaN(a) || Float.isNaN(b)) {
      assertNotXor(Float.isNaN(a), Float.isNaN(b));
    }
    else if (Float.isInfinite(a) || Float.isInfinite(b)) {
      assertNotXor(Float.POSITIVE_INFINITY == a, Float.POSITIVE_INFINITY == b);
      assertNotXor(Float.NEGATIVE_INFINITY == a, Float.NEGATIVE_INFINITY == b);
    }
    else {
      final int maxSigDigitIndex = maxSignifigantDigitIndex(a, b);
      final float absoluteDifference = abs(a - b);
      final int differenceSigDigitIndex = mostSignifigantDigitIndex(absoluteDifference);
      assertTrue("Assertion failed: |" + a + " - " + b + "| = " + absoluteDifference,
              maxSigDigitIndex > differenceSigDigitIndex
              && maxSigDigitIndex - differenceSigDigitIndex >= DEFAULT_PRECISION);
    }
  }

  protected static void assertApproximatelyEqual(final Double a, final Double b) {
    if (a == null || b == null) {
      assertSame(a, b);
    }
    else {
      assertApproximatelyEqual(a.doubleValue(), b.doubleValue());
    }
  }

  private static void assertApproximatelyEqual(final double a, final double b) {
    if (Double.isNaN(a) || Double.isNaN(b)) {
      assertNotXor(Double.isNaN(a), Double.isNaN(b));
    }
    else if (Double.isInfinite(a) || Double.isInfinite(b)) {
      assertNotXor(Double.POSITIVE_INFINITY == a, Double.POSITIVE_INFINITY == b);
      assertNotXor(Double.NEGATIVE_INFINITY == a, Double.NEGATIVE_INFINITY == b);
    }
    else {
      final int maxSigDigitIndex = maxSignifigantDigitIndex(a, b);
      final double absoluteDifference = abs(a - b);
      final int differenceSigDigitIndex = mostSignifigantDigitIndex(absoluteDifference);
      assertTrue("Assertion failed: |" + a + " - " + b + "| = " + absoluteDifference,
              maxSigDigitIndex > differenceSigDigitIndex
              && maxSigDigitIndex - differenceSigDigitIndex >= DEFAULT_PRECISION);
    }
  }

  private static void assertNotXor(final boolean a, final boolean b) {
    assertFalse(a ^ b);
  }

  /**
   * Formats a failure message of the form "expected: <i>expect</i>; but was: <i>got</i>". Does not
   * cause a test failure. You still have to call Assert.fail() if you want that.
   *
   * @param expect
   *          The expected value.
   * @param got
   *          The actual value.
   *
   * @return A new String as described above.
   */
  protected static String failMessage(final Object expect, final Object got) {
    return "expected: " + expect + "; but was: " + got;
  }

  protected org.slf4j.Logger logger;

  static {
    System.out.println("REMEMBER! Bus tests will not succeed if: \n" +
        "1. You do not run the unit tests with the flag: -Dorg.jboss.errai.bus.do_long_poll=false \n" +
        "2. You do not have the main and test source directories in the runtime classpath");

  }

  @Override
  protected void gwtSetUp() throws Exception {
    // Only setup handlers for first test.
    if (LoggingHandlerConfigurator.get() == null) {
      GWT.log("Initializing Logging for tests.");
      // Cannot use console logger in non-production compiled tests.
      new LoggingHandlerConfigurator().onModuleLoad();
      if (!GWT.isScript()) {
        GWT.log("Tests not running as a compiled script: disabling all but system handler.");
        final Handler[] handlers = Logger.getLogger("").getHandlers();
        for (final Handler handler : handlers) {
          handler.setLevel(Level.OFF);
        }
        LoggingHandlerConfigurator.get().getHandler(ErraiSystemLogHandler.class).setLevel(Level.ALL);
      }
    }

    logger = LoggerFactory.getLogger(getClass());
    bus = (ClientMessageBus) ErraiBus.get();

    InitVotes.setTimeoutMillis(60000);

    if (!(TaskManagerFactory.get() instanceof ClientTaskManager)) {
      TaskManagerFactory.setTaskManagerProvider(new TaskManagerProvider() {
        private final ClientTaskManager clientTaskManager = new ClientTaskManager();

        @Override
        public TaskManager get() {
          return clientTaskManager;
        }
      });
    }
    logger.info("Starting InitVotes polling from gwtSetup...");
    InitVotes.startInitPolling();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    try {
      logger.info("Stopping bus in gwtTearDown...");
      bus.stop(true);
      logger.info("Resetting InitVotes in gwtTearDown...");
      InitVotes.reset();
    } catch (final Throwable t) {
      logger.error("Encountered an error in gwtTearDown.", t);
      throw t;
    }
  }

  /**
   * Invokes the given Runnable after the bus has finished initializing (it's
   * online and connected to the server). The test timeout is 45 seconds.
   * <p/>
   * You must call {@link #finishTest()} within your runnable, or the test will
   * time out.
   *
   * @param r
   *     the stuff to run once the bus is online.
   */
  protected void runAfterInit(final Runnable r) {
    runAfterInit(45000, r);
  }

  /**
   * Invokes the given Runnable after the bus has finished initializing (it's
   * online and connected to the server).
   * <p/>
   * You must call {@link #finishTest()} within your runnable before the given
   * timeout has elapsed, or the test will fail with a time out.
   *
   * @param r
   *          the stuff to run once the bus is online.
   */
  protected void runAfterInit(final int timeoutMillis, final Runnable r) {
    delayTestFinish(timeoutMillis);
    InitVotes.registerOneTimeInitCallback(r);
  }

}
