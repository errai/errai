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
  public static final String USERS_PROPERTY_FILE = "application-users.properties";
  public static final String ROLES_PROPERTY_FILE = "application-roles.properties";
  public static final String STANDALONE_CONFIGURATION = "standalone" + File.separator + "configuration";

  public static String getJBossHome(final StackTreeLogger logger) throws UnableToCompleteException {
    final String JBOSS_HOME = System.getProperty(JBOSS_HOME_PROPERTY);

    if (JBOSS_HOME == null || JBOSS_HOME.equals("")) {
      logger.log(
              Type.ERROR,
              String.format(
                      "No value for %s was given: The root directory of your Jboss installation must be "
                      + "provided through the property %s in your pom.xml",
                      JBOSS_HOME_PROPERTY, JBOSS_HOME_PROPERTY));
      throw new UnableToCompleteException();
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
      logger.branch(Type.ERROR, String.format(
              "The errai.jboss.home directory, %s, does not appear to be home to a Jboss or Wildfly instance.",
              JBOSS_HOME));

      for (int i = 0; i < files.length; i++) {
        if (!files[i].exists()) {
          logger.log(Type.ERROR, String.format("%s not found.", files[i].getAbsolutePath()));
        }
      }
      logger.unbranch();

      throw new UnableToCompleteException();
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
