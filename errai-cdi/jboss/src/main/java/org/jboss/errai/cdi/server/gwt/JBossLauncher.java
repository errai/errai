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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.jboss.errai.cdi.server.as.JBossServletContainerAdaptor;
import org.jboss.errai.cdi.server.gwt.util.SimpleTranslator;
import org.jboss.errai.cdi.server.gwt.util.SimpleTranslator.AttributeEntry;
import org.jboss.errai.cdi.server.gwt.util.SimpleTranslator.Tag;
import org.jboss.errai.cdi.server.gwt.util.StackTreeLogger;
import org.jboss.errai.cdi.server.gwt.util.JBossUtil;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.ServletContainerLauncher;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * For starting a {@link JBossServletContainerAdaptor} controlling a standalone
 * Jboss/Wildfly AS.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class JBossLauncher extends ServletContainerLauncher {

  // Property names
  private final String JBOSS_DEBUG_PORT_PROPERTY = "errai.jboss.debug.port";
  private final String TEMPLATE_CONFIG_FILE_PROPERTY = "errai.jboss.config.file";
  private final String CLASS_HIDING_JAVA_AGENT_PROPERTY = "errai.jboss.javaagent.path";
  private final String JBOSS_JAVA_OPTS_PROPERTY = "errai.jboss.javaopts";
  private final String TMP_CONFIG_FILE = "standalone-errai-dev.xml";
  
  private final String JBOSS_HTTP_REMOTING_ADDRESS = "errai.jboss.httpremotingaddress";
  private final String JBOSS_NATIVE_REMOTING_ADDRESS = "errai.jboss.nativeremotingaddress";

  private StackTreeLogger logger;

  @Override
  public ServletContainer start(TreeLogger treeLogger, int port, File appRootDir) throws BindException, Exception {
    logger = new StackTreeLogger(treeLogger);

    logger.branch(Type.INFO, "Server launcher starting..");

    // Get properties
    final String DEBUG_PORT = System.getProperty(JBOSS_DEBUG_PORT_PROPERTY, "8001");
    final String TEMPLATE_CONFIG_FILE = System.getProperty(TEMPLATE_CONFIG_FILE_PROPERTY, "standalone-full.xml");
    final String CLASS_HIDING_JAVA_AGENT = System.getProperty(CLASS_HIDING_JAVA_AGENT_PROPERTY);
    String JAVA_OPTS = System.getProperty(JBOSS_JAVA_OPTS_PROPERTY, "");
    
    final String HTTP_REMOTING_ADDRESS = System.getProperty(JBOSS_HTTP_REMOTING_ADDRESS, "http-remoting://localhost:9990");
    final String NATIVE_REMOTING_ADDRESS = System.getProperty(JBOSS_NATIVE_REMOTING_ADDRESS, "remote://localhost:9999");

    final String jbossHome = JBossUtil.getJBossHome(logger);
    validateClassHidingJavaAgent(CLASS_HIDING_JAVA_AGENT);

    try {
      createTempConfigFile(TEMPLATE_CONFIG_FILE, TMP_CONFIG_FILE, jbossHome, port);
      logger.log(Type.INFO,
              String.format("Created temporary config file %s, copied from %s.", TMP_CONFIG_FILE, TEMPLATE_CONFIG_FILE));
    } 
    catch (IOException e) {
      logger.log(
              Type.ERROR,
              String.format("Unable to create temporary config file %s from %s", TMP_CONFIG_FILE, TEMPLATE_CONFIG_FILE),
              e);
    }

    final String JBOSS_START = JBossUtil.getStartScriptName(jbossHome);

    Process process;
    try {
      logger.branch(Type.INFO, String.format("Preparing JBoss AS instance (%s)", JBOSS_START));
      final File startScript = new File(JBOSS_START);
      if (!startScript.canExecute() && !startScript.setExecutable(true)) {
        logger.log(Type.ERROR, "Can not execute " + JBOSS_START);
        throw new UnableToCompleteException();
      }
      ProcessBuilder builder = new ProcessBuilder(JBOSS_START, "-c", TMP_CONFIG_FILE);

      logger.log(Type.INFO, String.format("Adding JBOSS_HOME=%s to instance environment", jbossHome));
      // Necessary for JBoss AS instance to startup
      builder.environment().put("JBOSS_HOME", jbossHome);

      // Allows JVM to be debugged
      builder.environment().put(
              "JAVA_OPTS",
              String.format("%s -Xrunjdwp:transport=dt_socket,address=%s,server=y,suspend=n -javaagent:%s", JAVA_OPTS,
                      DEBUG_PORT, CLASS_HIDING_JAVA_AGENT).trim());

      process = builder.start();

      logger.log(Type.INFO, "Redirecting stdout and stderr to share with this process");
      inheritIO(process.getInputStream(), System.out);
      inheritIO(process.getErrorStream(), System.err);

      logger.log(Type.INFO, "Executing AS instance...");
    }
    catch (IOException e) {
      logger.log(TreeLogger.Type.ERROR, "Failed to start JBoss AS process", e);
      logger.unbranch();
      throw new UnableToCompleteException();
    }

    logger.unbranch();

    logger.branch(Type.INFO, "Creating servlet container controller...");

    try {
      
      JBossServletContainerAdaptor controller = new JBossServletContainerAdaptor(port, appRootDir, JBossUtil.getDeploymentContext(), logger.peek(), process, HTTP_REMOTING_ADDRESS, NATIVE_REMOTING_ADDRESS);
      
      logger.log(Type.INFO, "Controller created");
      logger.unbranch();
      return controller;
    }
    catch (UnableToCompleteException e) {
      logger.log(Type.ERROR, "Could not start servlet container controller", e);
      throw new UnableToCompleteException();
    }
  }

  private void validateClassHidingJavaAgent(final String CLASS_HIDING_JAVA_AGENT) throws UnableToCompleteException {
    if (CLASS_HIDING_JAVA_AGENT == null) {
      logger.log(
              Type.ERROR,
              String.format(
                      "The local path to the artifact errai.org.jboss:class-local-class-hider:jar must be given as the property %s",
                      CLASS_HIDING_JAVA_AGENT_PROPERTY));
      throw new UnableToCompleteException();
    }
  }

  private void createTempConfigFile(String fromName, String toName, String jBossHome, int port) throws IOException,
          UnableToCompleteException {
    File configDir = new File(jBossHome, JBossUtil.STANDALONE_CONFIGURATION);
    File from = new File(configDir, fromName);
    File to = new File(configDir, toName);

    if (!from.exists()) {
      logger.log(
              Type.ERROR,
              String.format(
                      "Config file %s does not exit. It must be created or another one must be specified with the %s JVM property.",
                      from.getAbsolutePath(), TEMPLATE_CONFIG_FILE_PROPERTY));
      throw new UnableToCompleteException();
    }

    if (to.exists()) {
      logger.log(Type.WARN,
              String.format("Temporary config file %s already exists and will be deleted", to.getAbsolutePath()));
      to.delete();
    }

    to.createNewFile();
    to.deleteOnExit();

    InputStream inStream = new FileInputStream(from);
    OutputStream outStream = new FileOutputStream(to);

    // Replace default http port with provided port
    SimpleTranslator trans = new SimpleTranslator();
    trans.addFilter(new Tag("socket-binding", new AttributeEntry("name", "http")));
    trans.addNewTag("socket-binding-group", new Tag("socket-binding", new AttributeEntry("name", "http"),
            new AttributeEntry("port", String.valueOf(port))));

    try {
      trans.translate(inStream, outStream);
    }
    catch (XMLStreamException e) {
      logger.log(Type.ERROR, "Could not create copy of configuration from " + from.getAbsolutePath(), e);
      throw new UnableToCompleteException();
    }
    finally {
      inStream.close();
      outStream.close();
    }
  }

  private void inheritIO(final InputStream in, final OutputStream to) {
    new Thread() {
      @Override
      public void run() {
        try {
          IOUtils.copy(in, to);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }.start();
  }
}
