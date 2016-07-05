/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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
import static java.lang.Math.floor;
import static java.lang.Math.log10;
import static java.lang.Math.max;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class FloatUtil {

  private FloatUtil() {}

  public static final int DEFAULT_PRECISION = 2;

  public static int mostSignifigantDigitIndex(final float a) {
    return Double.valueOf(floor(log10(a))).intValue();
  }

  public static int mostSignifigantDigitIndex(final double a) {
    return Double.valueOf(floor(log10(a))).intValue();
  }

  public static int maxSignifigantDigitIndex(final float a, final float b) {
    return max(mostSignifigantDigitIndex(a), mostSignifigantDigitIndex(b));
  }

  public static int maxSignifigantDigitIndex(final double a, final double b) {
    return max(mostSignifigantDigitIndex(a), mostSignifigantDigitIndex(b));
  }

  public static int differenceSignifigantDigitIndex(final float a, final float b) {
    return mostSignifigantDigitIndex(abs(a - b));
  }

  public static int differenceSignifigantDigitIndex(final double a, final double b) {
    return mostSignifigantDigitIndex(abs(a - b));
  }

  public static boolean withinPrecision(final double a, final double b) {
    final int maxSignifigantDigitIndex = maxSignifigantDigitIndex(a, b);
    final int differenceSignifigantDigitIndex = differenceSignifigantDigitIndex(a, b);
    return maxSignifigantDigitIndex > differenceSignifigantDigitIndex
            && maxSignifigantDigitIndex - differenceSignifigantDigitIndex >= DEFAULT_PRECISION;
  }

  public static boolean withinPrecision(final float a, final float b) {
    final int maxSignifigantDigitIndex = maxSignifigantDigitIndex(a, b);
    final int differenceSignifigantDigitIndex = differenceSignifigantDigitIndex(a, b);
    return maxSignifigantDigitIndex > differenceSignifigantDigitIndex
            && maxSignifigantDigitIndex - differenceSignifigantDigitIndex >= DEFAULT_PRECISION;
  }

}
