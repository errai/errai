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

package org.jboss.errai.otec.util;

/**
 * @author Christian Sadilek
 * @author Mike Brock
 */
public class OTLogFormat {
  public static final String LOG_FORMAT = "%-9s %-10s %-10s %-70s %-4s %-30s\n";

  public static void printLogTitle() {
    System.out.printf(OTLogFormat.LOG_FORMAT, "TYPE", "FROM", "TO", "MUTATIONS", "REV", "STATE");
    System.out.println(repeat('-', 140));
  }

  public static void log(String type, String mutations, String from, String to, int rev, String state) {
    synchronized (OTLogFormat.class) {
      System.out.printf(LOG_FORMAT, type, from, to, mutations, rev, state);
      System.out.flush();
    }
  }

  private static String repeat(char c, int amount) {
    final StringBuilder builder = new StringBuilder(amount);
    for (int i = 0; i < amount; i++) {
      builder.append(c);
    }
    return builder.toString();
  }
}
