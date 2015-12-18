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

package org.jboss.errai.cdi.server.gwt;

import java.io.*;
import java.net.BindException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.StandaloneServer;
import org.jboss.errai.cdi.server.as.JBossServletContainerAdaptor;
import org.jboss.errai.cdi.server.gwt.util.JBossUtil;
import org.jboss.errai.cdi.server.gwt.util.StackTreeLogger;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.ServletContainerLauncher;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * A {@link ServletContainerLauncher} controlling an embedded WildFly instance.
 *
 * This launcher will add exclusions for client-side classes to the project's
 * beans.xml. These classes are not needed on the server and are going to cause
 * class loading issues if deployed (i.e. because they reference GWT classes
 * that are not present at runtime). By default, classes in packages under
 * /client/local will be considered client-side but this can be configured using
 * the system property errai.client.local.class.pattern. Existing exclusions
 * will stay intact. If no beans.xml is present, a new one will be created.
 *
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */
public class EmbeddedWildFlyLauncher extends ServletContainerLauncher {
  private static final String CLIENT_LOCAL_CLASS_PATTERN_PROPERTY = "errai.client.local.class.pattern";
  private static final String DEFAULT_CLIENT_LOCAL_CLASS_PATTERN = ".*/client/local/.*";

  private static final String ERRAI_SCANNER_HINT_START =
          "\n    <!-- These exclusions were added by Errai to avoid deploying client-side classes to the server -->\n";
  private static final String ERRAI_SCANNER_HINT_END =
          "    <!-- End of Errai exclusions -->\n";

  private static final String DEVMODE_BEANS_XML_TEMPLATE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\">\n" +
          "  <scan>" +
          "$EXCLUSIONS" +
          "  </scan>\n" +
       "</beans>";

  private static final String DEVMODE_BEANS_XML_EXCLUSION_TEMPLATE = "    <exclude name = \"$CLASSNAME\" />\n";

  private static final String ERRAI_PROPERTIES_HINT_START = "#Errai-Start\n";
  private static final String ERRAI_PROPERTIES_HINT_END = "\n#Errai-End\n";
  private static final String ERRAI_PROPERTIES_REALM_TOKEN = "\n#$REALM_NAME=ApplicationRealm$ This line is used by " +
          "the add-user utility to identify the realm name already used in this file.\n";

  static {
    System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
  }

  private StackTreeLogger logger;

  @Override
  public ServletContainer start(final TreeLogger treeLogger, final int port, final File appRootDir) throws BindException, Exception {
    logger = new StackTreeLogger(treeLogger);
    try {
      System.setProperty("jboss.http.port", "" + port);

      final String jbossHome = JBossUtil.getJBossHome(logger);
      final String[] cmdArgs = JBossUtil.getCommandArguments(logger);
      final StandaloneServer embeddedWildFly = EmbeddedServerFactory.create(jbossHome, null, null, new String[0], cmdArgs);
      embeddedWildFly.start();

      prepareBeansXml(appRootDir);
      prepareUsersAndRoles(jbossHome);
      JBossServletContainerAdaptor controller = new JBossServletContainerAdaptor(port, appRootDir,
              JBossUtil.getDeploymentContext(), logger.peek(), null);

      return controller;
    }
    catch (UnableToCompleteException e) {
      logger.log(Type.ERROR, "Could not start servlet container controller", e);
      throw new UnableToCompleteException();
    }
  }

  /**
   * Reads application-users.properties and application-roles.properties from
   * the classpath and amends the corresponding files under
   * standalone/configuration. This allows applications to specify users and
   * roles for development mode.
   */
  private void prepareUsersAndRoles(final String jbossHome) {
    InputStream usersStream =
            Thread.currentThread().getContextClassLoader().getResourceAsStream(JBossUtil.USERS_PROPERTY_FILE);

    if (usersStream != null) {
      processPropertiesFile(JBossUtil.USERS_PROPERTY_FILE, jbossHome, usersStream, true);
    }

    InputStream rolesStream =
            Thread.currentThread().getContextClassLoader().getResourceAsStream(JBossUtil.ROLES_PROPERTY_FILE);

    if (rolesStream != null) {
      processPropertiesFile(JBossUtil.ROLES_PROPERTY_FILE, jbossHome, rolesStream, false);
    }
  }

  private void processPropertiesFile(final String propertyFileName, final String jbossHome,
                                           final InputStream newUsersStream, final boolean handleRealmToken) {
    final File propertyDir = new File(jbossHome, JBossUtil.STANDALONE_CONFIGURATION);
    final File propertyFile = new File(propertyDir, propertyFileName);

    try {

      String realmToken = ERRAI_PROPERTIES_REALM_TOKEN;
      boolean isErraiContent = false;
      final StringBuilder result = new StringBuilder();
      List<String> lines = FileUtils.readLines(propertyFile);
      if (lines != null) {

        for (final String currentLine : lines) {
          String trimmed = currentLine.trim();
          if(trimmed.startsWith(ERRAI_PROPERTIES_HINT_START.trim())) {
            isErraiContent = true;
          } else if(trimmed.startsWith(ERRAI_PROPERTIES_HINT_END.trim())) {
            isErraiContent = false;
          } else if(handleRealmToken && trimmed.startsWith("#") && trimmed.contains("$REALM_NAME=")) {
            realmToken = currentLine;
          } else if (!isErraiContent) {
            result.append(currentLine).append("\n");
          }
        }

        final String newUsersStr = IOUtils.toString(newUsersStream, (String) null);
        result.append(ERRAI_PROPERTIES_HINT_START);
        result.append(newUsersStr).append("\n");
        result.append(ERRAI_PROPERTIES_HINT_END);

        if (handleRealmToken) {
          result.append(realmToken).append("\n");
        }

        try {
          FileUtils.write(propertyFile, result.toString());
        } catch (IOException e) {
          throw new RuntimeException("Failed to write content for " +
                  propertyFileName + " in " + propertyFile.getAbsolutePath(), e);
        }
      }

    } catch (IOException e) {
      throw new RuntimeException("Failed to parse content for " +
              propertyFileName + " in " + propertyFile.getAbsolutePath(), e);
    }

  }



  /**
   * Writes a new or updates an existing beans.xml file to add exclusions for
   * client-only classes. We do this to avoid class loading problems on
   * deployment.
   *
   * @param appRootDir
   *          the root application directory (configured as <hostedWebapp> when
   *          using the gwt-maven-plugin).
   * @throws IOException
   */
  private void prepareBeansXml(File appRootDir) throws IOException {
    File webInfDir = new File(appRootDir, "WEB-INF");
    File classesDir = new File(webInfDir, "classes");

    StringBuilder exclusions = new StringBuilder();
    exclusions.append(ERRAI_SCANNER_HINT_START);
    for (File clientLocalClass : FileUtils.listFiles(appRootDir, new ClientLocalFileFilter(), TrueFileFilter.INSTANCE)) {
      final String className = clientLocalClass.getAbsolutePath();
      if (className.endsWith(".class")) {
        String exclusion = className
          .replace(classesDir.getAbsolutePath(), "")
          .replace(".class", "")
          .replace(File.separator, ".")
          .substring(1);
        exclusions.append(DEVMODE_BEANS_XML_EXCLUSION_TEMPLATE.replace("$CLASSNAME", exclusion));
      }
    }
    exclusions.append(ERRAI_SCANNER_HINT_END);

    File beansXml = new File(webInfDir, "beans.xml");
    String beansXmlContent;
    if (!beansXml.exists()) {
      beansXmlContent = DEVMODE_BEANS_XML_TEMPLATE.replace("$EXCLUSIONS", exclusions.toString());
    }
    else {
      beansXmlContent = FileUtils.readFileToString(beansXml);
      beansXmlContent = removeExistingErraiExclusions(beansXmlContent);
      if (beansXmlContent.contains(exclusions))
        return;

      if (beansXmlContent.contains("<scan>")) {
        beansXmlContent = beansXmlContent.replace("<scan>", "<scan>" + exclusions);
      }
      else {
        beansXmlContent = beansXmlContent.replace("</beans>", "  <scan>" + exclusions + "  </scan>\n</beans>");
      }
      validateBeansXml(beansXmlContent);
    }
    FileUtils.write(beansXml, beansXmlContent);
  }

  private void validateBeansXml(String beansXmlContent) {
    if (beansXmlContent.contains("beans_1_0.xsd")) {
      logger.log(Type.WARN, "Your beans.xml file doesn't not allow for CDI 1.1! "
              + "Please remove the CDI 1.0 XML Schema.");
    }
  }

  private String removeExistingErraiExclusions(String beansXmlContent) {
    String oldExclusions = StringUtils.substringBetween(beansXmlContent,
            ERRAI_SCANNER_HINT_START, ERRAI_SCANNER_HINT_END);

    beansXmlContent = beansXmlContent.replace(ERRAI_SCANNER_HINT_START, "");
    if (oldExclusions != null) {
      beansXmlContent = beansXmlContent.replace(oldExclusions, "");
    }
    beansXmlContent = beansXmlContent.replace(ERRAI_SCANNER_HINT_END, "");

    return beansXmlContent;
  }

  private class ClientLocalFileFilter implements IOFileFilter {
    final String clientLocalClassPatternString =
            System.getProperty(CLIENT_LOCAL_CLASS_PATTERN_PROPERTY, DEFAULT_CLIENT_LOCAL_CLASS_PATTERN);

    final Pattern clientLocalClassPattern = Pattern.compile(clientLocalClassPatternString);

    @Override
    public boolean accept(File pathName) {
      return accept(pathName.getAbsolutePath());
    }

    @Override
    public boolean accept(File dir, String file) {
      String fullName = dir.getAbsolutePath() + File.separator + file;
      return accept(fullName);
    }

    private boolean accept(String fileName) {
      return clientLocalClassPattern.matcher(fileName).matches();
    }
  }
}
