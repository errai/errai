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

package org.jboss.errai.config.rebind;

/**
 * @author Mike Brock
 */
public abstract class EnvUtil {

  private static volatile Boolean _isJUnitTest;

  public static boolean isJUnitTest() {
    if (_isJUnitTest != null) {
      return _isJUnitTest;
    }

    for (final StackTraceElement el : new Throwable().getStackTrace()) {
      if (el.getClassName().startsWith("com.google.gwt.junit.client.") || el.getClassName().startsWith("org.junit")) {
        return _isJUnitTest = Boolean.TRUE;
      }
    }
    return _isJUnitTest = Boolean.FALSE;
  }

  private static volatile Boolean _isDevMode;

  public static boolean isDevMode() {
    if (_isDevMode != null) {
      return _isDevMode;
    }

    for (final StackTraceElement el : new Throwable().getStackTrace()) {
      if (el.getClassName().startsWith("com.google.gwt.dev.shell.OophmSessionHandler") || el.getClassName()
              .startsWith("com.google.gwt.dev.codeserver")) {
        return _isDevMode = Boolean.TRUE;
      }
    }
    return _isDevMode = Boolean.FALSE;
  }

  private static volatile Boolean _isProdMode;

  public static boolean isProdMode() {
    if (_isProdMode != null) {
      return _isProdMode;
    }

    return _isProdMode = !isDevMode() && !isJUnitTest();
  }

  public static void recordEnvironmentState() {
    isJUnitTest();
    isDevMode();
    isProdMode();
  }
}
