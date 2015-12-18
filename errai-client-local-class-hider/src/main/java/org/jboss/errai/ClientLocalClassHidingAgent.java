/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A Java Agent that ensures all attempts at loading a class whose package and
 * name match a particular pattern end up loading a trivial subclass of
 * java.lang.Object.
 * <p>
 * The motivation for creating this agent was to hide GWT client-local classes
 * from app servers: attempts to load these classes run into several kinds of
 * dependency resolution problems: client-side GWT API that does not exist on
 * the server, as well as missing native methods for JSNI.
 */
public class ClientLocalClassHidingAgent {

  private static final String DEFAULT_CLASS_NAME_PATTERN = ".*/client/local/.*";

  /**
   * Populates a map with default options, overriding and supplementing them
   * with those specified in the options string.
   *
   * @param options
   * @return
   */
  private static final Map<String, String> parseOptionsWithDefaults(String options) {
    Map<String, String> map = new HashMap<String, String>();

    if (options == null) options = "";

    // load map with defaults
    map.put("debugAgent", Boolean.FALSE.toString());
    map.put("classPattern", DEFAULT_CLASS_NAME_PATTERN);

    for (String keyval : options.split(",")) {
      String key;
      String val;
      int equalsIdx = keyval.indexOf("=");
      if (equalsIdx >= 0) {
        key = keyval.substring(0, equalsIdx);
        val = keyval.substring(equalsIdx + 1);
      } else {
        key = keyval;
        val = null;
      }
      map.put(key, val);
    }

    return map;
  }
  
  public static void premain(String agentArgs, Instrumentation inst) {
    Map<String, String> options = parseOptionsWithDefaults(agentArgs);
    boolean debug = Boolean.parseBoolean(options.get("debugAgent"));
    Pattern hideClassesPattern = Pattern.compile(options.get("classPattern"));

    inst.addTransformer(new ClientLocalClassHider(hideClassesPattern, debug));
    inst.addTransformer(new GwtLinkerClassHider(debug));
  }
}
