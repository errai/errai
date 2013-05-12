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

package org.jboss.errai.otec.harness;

import com.google.gwt.core.client.GWT;
import org.jboss.errai.otec.client.util.OTLogAdapter;

/**
 * @author Christian Sadilek
 * @author Mike Brock
 */
public class OTTestingLogger implements OTLogAdapter {
  private static final String clientFilter = System.getProperty("otec.log.clientFilter");


  @Override
  public void printLogTitle() {
    System.out.printf(OTLogAdapter.LOG_FORMAT, "TYPE", "FROM", "TO", "MUTATIONS", "REV", "STATE");
    System.out.println(repeat('-', 140));
  }

  @Override
  public boolean log(final String type, final String mutations, final String from, final String to, final int rev, final String state) {

    synchronized (OTTestingLogger.class) {
      if (clientFilter != null) {
        if (!clientFilter.equals(to)) {
          return true;
        }
      }

      if (!GWT.isClient()) {
        System.out.printf(LOG_FORMAT, type, from, to, mutations, rev, state);
        System.out.flush();
      }
      return true;
    }
  }

  @Override
  public boolean log(final String message) {
    System.out.println(message);
    return true;
  }

  public static String repeat(final char c, final int amount) {
    final StringBuilder builder = new StringBuilder(amount);
    for (int i = 0; i < amount; i++) {
      builder.append(c);
    }
    return builder.toString();
  }


}
