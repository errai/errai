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

package org.jboss.errai.codegen.framework.tests;

/**
 * @author Mike Brock
 */
public final class ExceptionUtil {
  public static Throwable getRootCause(Throwable e) {
    Throwable c = e;
    while (c.getCause() != null) c = c.getCause();
    return c;
  }

  public static Throwable getIntermediateCause(Throwable root, Class<? extends Throwable> cause) {
    Throwable c = root;
    do {
      if (cause.isAssignableFrom(c.getClass())) return c;
    }
    while ((c = c.getCause()) != null);

    return null;
  }

  public static boolean isIntermediateCause(Throwable root, Class<? extends Throwable> cause) {
    return getIntermediateCause(root, cause) != null;
  }
}
