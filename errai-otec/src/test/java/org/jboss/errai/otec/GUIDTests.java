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

package org.jboss.errai.otec;

import org.jboss.errai.otec.client.util.GUIDUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GUIDTests {
  @Test @Ignore
  public void testGUIDUtilEntropy() throws Throwable {
    final Set<String> set = new HashSet<String>(100000000, 1.0f);

    final List<Thread> liveThreads = new ArrayList<Thread>();

    for (int e = 0; e < 4; e++) {
      final Thread thread = new Thread() {
        Set<String> localSet = new HashSet<String>(200000);

        @Override
        public void run() {
          for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (i % 2500000 == 0) {
              synchronized (set) {
                System.out.print("[>] -> ");

                for (final String s : localSet) {
                  if (!set.add(s)) {
                    throw new RuntimeException("collision: " + s);
                  }
                }

                System.out.print(i + " :: mem usage: " + Runtime.getRuntime().totalMemory() / (1024 * 1024)
                    + "MB :: total GUIDs: " + set.size());

                localSet = new HashSet<String>(200000);

                System.out.println(" -> [>]");
              }
            }

            if (!localSet.add(GUIDUtil.createGUID())) {
              throw new RuntimeException("collision: (# " + i + ") >>");
            }
          }
        }
      };

      liveThreads.add(thread);

      thread.setPriority(Thread.MAX_PRIORITY);
      thread.start();
      Thread.sleep(100);
    }

    for (final Thread thread : liveThreads) {
      thread.join();
    }
  }
}
