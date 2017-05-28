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

package org.jboss.errai.cdi.server.gwt.util;

import java.io.File;

import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * Utility methods for bootstrapping various JBoss/WildFly containers in
 * DevMode.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JBossUtil {
  private static final String APP_CONTEXT_PROPERTY = "errai.dev.context";
  private static final String JBOSS_HOME_PROPERTY = "errai.jboss.home";
  private static final String CMD_ARGS_PROPERTY = "errai.jboss.args";
  public static final String APP_USERS_PROPERTY_FILE = "application-users.properties";
  public static final String APP_ROLES_PROPERTY_FILE = "application-roles.properties";
  public static final String MGMT_USERS_PROPERTY_FILE = "mgmt-users.properties";
  public static final String MGMT_GROUPS_PROPERTY_FILE = "mgmt-groups.properties";
  public static final String CLI_CONFIGURATION_FILE = "bin" + File.separator + "jboss-cli.xml";
  public static final String STANDALONE_CONFIGURATION = "standalone" + File.separator + "configuration";

  public static String getJBossHome(final StackTreeLogger logger) throws UnableToCompleteException {
    final String JBOSS_HOME = System.getProperty(JBOSS_HOME_PROPERTY);

    if (JBOSS_HOME == null || JBOSS_HOME.equals("")) {
      throw new IllegalStateException(
              String.format(
                      "No value for %s was given: The root directory of your Jboss installation must be "
                      + "provided through the property %s in your pom.xml.",
                      JBOSS_HOME_PROPERTY, JBOSS_HOME_PROPERTY));
    }

    /*
     * Check that start script and configuration folder exist.
     */
    final File[] files = new File[] { new File(JBOSS_HOME), new File(getStartScriptName(JBOSS_HOME)),
        new File(JBOSS_HOME, STANDALONE_CONFIGURATION) };

    boolean isValid = true;
    for (int i = 0; i < files.length; i++) {
      if (!files[i].exists()) {
        isValid = false;
        break;
      }
    }

    if (!isValid) {
      StringBuilder s = new StringBuilder();
      s.append(String.format(
              "The %s directory (%s) does not appear to be home to a JBoss or Wildfly instance.\n",
              JBOSS_HOME_PROPERTY, JBOSS_HOME));

      for (int i = 0; i < files.length; i++) {
        if (!files[i].exists()) {
          s.append(String.format("  %s not found.\n", files[i].getAbsolutePath()));
        }
      }

      throw new IllegalStateException(s.toString());
    }

    return JBOSS_HOME;
  }

  public static String getStartScriptName(String jbossHome) {
    final String script = System.getProperty("os.name").toLowerCase().contains("windows") ? "standalone.bat"
            : "standalone.sh";

    return String.format("%s%cbin%c%s", jbossHome, File.separatorChar, File.separatorChar, script);
  }

  public static String getDeploymentContext() {
    return System.getProperty(APP_CONTEXT_PROPERTY, "ROOT");
  }

  public static String[] getCommandArguments(StackTreeLogger logger) {
    final String rawArgs = System.getProperty(CMD_ARGS_PROPERTY);

    if (rawArgs == null) {
      return new String[0];
    } else {
      return rawArgs.split("\\s+");
    }
  }

}
