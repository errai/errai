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

import org.jboss.errai.otec.client.mutation.MutationType;

/**
 * @author Mike Brock
 */
public final class DiffUtil {
  private DiffUtil() {
  }

  /**
   * A simple diff algorithm that assumes only one contiguous block has changed. This algorithm cannot handle
   * interleaved differences in a string.
   *
   * @param a
   * @param b
   *
   * @return
   */
  public static Delta diff(final String a, final String b) {
    int i = 0, y = 0;
    final int maxLen = Math.min(a.length(), b.length());

    int diffStart = -1;

    for (; i < maxLen; ) {
      if (a.charAt(i) == b.charAt(y)) {
        i++;
        y++;
      }
      else {
        diffStart = i;
        break;
      }
    }

    final int lenDiff = b.length() - a.length();

    if (diffStart == -1 && lenDiff == 0) {
      return Delta.of(MutationType.Noop, 0, "");
    }
    else {
      if (lenDiff < 0) {
        if (diffStart == -1) {
          diffStart = 0;
        }

        //delete
        return Delta.of(MutationType.Delete, diffStart, a.substring(diffStart, diffStart + -lenDiff));
      }
      else if (lenDiff == 0) {
        //replace -- this is not currently supportable.
        return null;
      }
      else {
        if (diffStart == -1) {
          diffStart = a.length();
        }

        //insert
        return Delta.of(MutationType.Insert, diffStart, b.substring(diffStart, diffStart + lenDiff));
      }
    }
  }

  public static class Delta {
    private final MutationType type;
    private final int cursor;
    private final String deltaText;

    private Delta(final MutationType type, final int cursor, final String deltaText) {
      this.type = type;
      this.cursor = cursor;
      this.deltaText = deltaText;
    }

    public static Delta of(final MutationType type, final int cursor, final String deltaText) {
      return new Delta(type, cursor, deltaText);
    }

    public MutationType getType() {
      return type;
    }

    public int getCursor() {
      return cursor;
    }

    public String getDeltaText() {
      return deltaText;
    }
  }
}
