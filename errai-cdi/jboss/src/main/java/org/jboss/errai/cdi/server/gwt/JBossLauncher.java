package org.jboss.errai.cdi.server.gwt;

import java.io.File;
import java.io.IOException;
import java.net.BindException;

import org.jboss.errai.cdi.server.as.JBossServletContainerAdaptor;
import org.jboss.errai.cdi.server.gwt.util.CopyUtil;
import org.jboss.errai.cdi.server.gwt.util.StackTreeLogger;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.ServletContainerLauncher;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;

public class JBossLauncher extends ServletContainerLauncher {

  // Property names
  private final String JBOSS_HOME_PROPERTY = "errai.jboss.home";
  private final String JBOSS_DEBUG_PORT_PROPERTY = "errai.jboss.debug.port";
  private final String TEMPLATE_CONFIG_FILE_PROPERTY = "errai.jboss.config.file";
  private final String CLASS_HIDING_JAVA_AGENT_PROPERTY = "errai.jboss.javaagent.path";

  private final String TMP_CONFIG_FILE = "standalone-errai-dev.xml";

  StackTreeLogger logger;

  @Override
  public ServletContainer start(TreeLogger treeLogger, int port, File appRootDir) throws BindException, Exception {
    logger = new StackTreeLogger(treeLogger);

    logger.branch(Type.INFO, "Starting launcher...");

    // Get properties
    final String JBOSS_HOME = System.getProperty(JBOSS_HOME_PROPERTY);
    final String DEBUG_PORT = System.getProperty(JBOSS_DEBUG_PORT_PROPERTY, "8001");
    final String TEMPLATE_CONFIG_FILE = System.getProperty(TEMPLATE_CONFIG_FILE_PROPERTY, "standalone-full.xml");
    final String CLASS_HIDING_JAVA_AGENT = System.getProperty(CLASS_HIDING_JAVA_AGENT_PROPERTY);

    if (JBOSS_HOME == null) {
      logger.log(Type.ERROR, String.format(
              "No value for %s was found: The root directory of your JBoss installation must be given to the JVM",
              JBOSS_HOME_PROPERTY));
      throw new UnableToCompleteException();
    }
    if (CLASS_HIDING_JAVA_AGENT == null) {
      logger.log(
              Type.ERROR,
              String.format(
                      "The local path the artifact errai.org.jboss:class-local-class-hider:jar must be given as the property %s",
                      CLASS_HIDING_JAVA_AGENT_PROPERTY));
      throw new UnableToCompleteException();
    }

    try {
      copyConfigFile(TEMPLATE_CONFIG_FILE, TMP_CONFIG_FILE, JBOSS_HOME);
    } catch (IOException e) {
      logger.log(
              Type.ERROR,
              String.format("Unable to create temporary config file %s from %s", TMP_CONFIG_FILE, TEMPLATE_CONFIG_FILE),
              e);
    }

    final String JBOSS_START = JBOSS_HOME + "/bin/" + getStartScriptName();

    Process process;
    try {
      logger.branch(Type.INFO, String.format("Preparing JBoss AS instance (%s)", JBOSS_START));
      ProcessBuilder builder = new ProcessBuilder(JBOSS_START, "-c", TMP_CONFIG_FILE);

      logger.log(Type.INFO, String.format("Adding JBOSS_HOME=%s to instance environment", JBOSS_HOME));
      // Necessary for JBoss AS instance to startup
      builder.environment().put("JBOSS_HOME", JBOSS_HOME);

      // Allows JVM to be debugged
      builder.environment().put(
              "JAVA_OPTS",
              String.format("-Xrunjdwp:transport=dt_socket,address=%s,server=y,suspend=n -javaagent:%s", DEBUG_PORT,
                      CLASS_HIDING_JAVA_AGENT));

      logger.log(Type.INFO, "Redirecting stdout and stderr to share with this process");
      builder.inheritIO();

      process = builder.start();
      logger.log(Type.INFO, "Executing AS instance...");
    } catch (IOException e) {
      logger.log(TreeLogger.Type.ERROR, "Failed to start JBoss AS process", e);
      logger.unbranch();
      throw new UnableToCompleteException();
    }

    // TODO figure out better way to identify when AS is up
    try {
      logger.log(Type.INFO, "Waiting for AS instance...");
      Thread.sleep(5000);
    } catch (InterruptedException e1) {
      // Don't care too much if this is interrupted... but I guess we'll log it
      logger.log(Type.WARN, "Launcher was interrupted while waiting for JBoss AS to start", e1);
    }
    logger.unbranch();

    logger.branch(Type.INFO, "Creating servlet container controller...");

    try {
      JBossServletContainerAdaptor controller = new JBossServletContainerAdaptor(port, appRootDir, logger.peek(),
              process);
      logger.log(Type.INFO, "Controller created");
      logger.unbranch();
      return controller;
    } catch (UnableToCompleteException e) {
      logger.log(Type.ERROR, "Could not start servlet container controller", e);
      throw new UnableToCompleteException();
    }
  }

  private void copyConfigFile(String fromName, String toName, String jBossHome) throws IOException,
          UnableToCompleteException {
    File configDir = new File(jBossHome, "standalone/configuration");
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

    CopyUtil.copyFile(to, from);
  }

  // TODO make portable
  private String getStartScriptName() {
    return "standalone.sh";
  }
}
