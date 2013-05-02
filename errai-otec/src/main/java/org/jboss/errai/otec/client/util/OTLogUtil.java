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

import org.jboss.errai.common.client.util.LogUtil;

/**
 * @author Mike Brock
 */
public class OTLogUtil {
  private static OTLogAdapter logAdapter = new OTLogAdapter() {
    @Override
    public void printLogTitle() {
    }

    @Override
    public boolean log(String type, String mutations, String from, String to, int rev, String state) {
      LogUtil.log(type + ":" + mutations + ";rev=" + rev );
      return true;
    }
  };

  public static void setLogAdapter(OTLogAdapter logAdapter) {
    OTLogUtil.logAdapter = logAdapter;
  }

  public static void printLogTitle() {
    logAdapter.printLogTitle();
  }

  public static boolean log(String type, String mutations, String from, String to, int rev, String state) {
    logAdapter.log(type, mutations, from, to, rev, state);
    return true;
  }
}
