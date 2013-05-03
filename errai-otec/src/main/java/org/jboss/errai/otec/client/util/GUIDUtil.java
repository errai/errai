/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.otec.client.util;

/**
 * Simple GUID utility that implements a modified form of the Linear Congruent Generator (LCG) algorithm,
 * to create psuedo-random GUIDs. The generator is double seeded by system time. Once when the GUIDUtil class
 * is loaded, and then its seeded again based on the time that createGUID is called.
 *
 * @author Mike Brock
 */
public final class GUIDUtil {
  private GUIDUtil() {
  }

  private static final char[] chars = "abcdef1234567890".toCharArray();

  private static final Object seedLock = new Object();
  private static final int MAX = Short.MAX_VALUE;
  private static volatile double rSeed = System.currentTimeMillis() % Short.MAX_VALUE;
  private static volatile int _a = 1;
  private static volatile int _b = 1000;

  public static String createGUID() {

    final double seed;

    final int q, p;
    synchronized (seedLock) {
      seed = ++rSeed;
      if (++_a == 100000) {
       _a = 1;
      }
      if (--_b == 0) {
        _b = (int) System.currentTimeMillis() % Short.MAX_VALUE;
      }
      q = _a;
      p = _b;
    }

    final long time = System.currentTimeMillis() + q;
    int a = (int) ((time & 0xFFFF) * 73278) + q, x, b = ((int) (time & 0xFFFF) * 33187) + p;

    if (b < 0) {
      b = -b;
    }
    else if (b == 0) {
      b = 2;
    }

    x = (int) seed * a / b;

    final char[] charArray = new char[35];

    for (int i = 0; i < 35; i++) {
      if (i != (charArray.length - 1) && i % 5 == 0) {
        charArray[i] = ':';
        continue;
      }

      x = (a * x + b) % MAX;
      a += time << 16 % MAX;
      b += time << 10 % MAX;

      int idx = x;
      if (idx < 0)
        idx = -idx;

      charArray[i] = chars[idx % chars.length];
    }

    return new String(charArray, 1, charArray.length - 1);
  }
}
