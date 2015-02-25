package org.jboss.errai.cdi.server.gwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.util.Properties;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.jboss.errai.cdi.server.as.JBossServletContainerAdaptor;
import org.jboss.errai.cdi.server.gwt.util.SimpleTranslator;
import org.jboss.errai.cdi.server.gwt.util.SimpleTranslator.AttributeEntry;
import org.jboss.errai.cdi.server.gwt.util.SimpleTranslator.Tag;
import org.jboss.errai.cdi.server.gwt.util.StackTreeLogger;

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
  private final String JBOSS_HOME_PROPERTY = "errai.jboss.home";
  private final String JBOSS_DEBUG_PORT_PROPERTY = "errai.jboss.debug.port";
  private final String TEMPLATE_CONFIG_FILE_PROPERTY = "errai.jboss.config.file";
  private final String CLASS_HIDING_JAVA_AGENT_PROPERTY = "errai.jboss.javaagent.path";
  private final String APP_CONTEXT_PROPERTY = "errai.dev.context";
  private final String JBOSS_JAVA_OPTS_PROPERTY = "errai.jboss.javaopts";
  private final String HIDDEN_PATTERN_PROPERTY = "errai.jboss.hidden.pattern";

  private final String TMP_CONFIG_FILE = "standalone-errai-dev.xml";
  private static final String STANDALONE_CONFIGURATION = "standalone" + File.separator + "configuration";

  StackTreeLogger logger;

  @Override
  public ServletContainer start(TreeLogger treeLogger, int port, File appRootDir) throws BindException, Exception {
    logger = new StackTreeLogger(treeLogger);

    logger.branch(Type.INFO, "Starting launcher...");

    // Get properties
    final String JBOSS_HOME = System.getProperty(JBOSS_HOME_PROPERTY);
    final String DEBUG_PORT = System.getProperty(JBOSS_DEBUG_PORT_PROPERTY, "8001");
    final String TEMPLATE_CONFIG_FILE = System.getProperty(TEMPLATE_CONFIG_FILE_PROPERTY, "standalone-full.xml");
    final String JAVA_AGENT = System.getProperty(CLASS_HIDING_JAVA_AGENT_PROPERTY);
    final String DEPLOYMENT_CONTEXT = System.getProperty(APP_CONTEXT_PROPERTY, "ROOT");
    final String JAVA_OPTS = System.getProperty(JBOSS_JAVA_OPTS_PROPERTY, "-XX:MaxPermSize=256m");
    final String HIDDEN_PATTERN = System.getProperty(HIDDEN_PATTERN_PROPERTY);
    final String CLASS_HIDING_JAVA_AGENT = resolveClassHidingJavaAgent( JAVA_AGENT, HIDDEN_PATTERN );

    validateJbossHome(JBOSS_HOME);

    try {
      createTempConfigFile(TEMPLATE_CONFIG_FILE, TMP_CONFIG_FILE, JBOSS_HOME, port);
      logger.log(Type.INFO,
              String.format("Created temporary config file %s, copied from %s.", TMP_CONFIG_FILE, TEMPLATE_CONFIG_FILE));
    }
    catch (IOException e) {
      logger.log(
              Type.ERROR,
              String.format("Unable to create temporary config file %s from %s", TMP_CONFIG_FILE, TEMPLATE_CONFIG_FILE),
              e);
    }

    final String JBOSS_START = getStartScriptName(JBOSS_HOME);

    Process process;
    try {
      logger.branch(Type.INFO, String.format("Preparing JBoss AS instance (%s)", JBOSS_START));
      final File startScript = new File(JBOSS_START);
      if (!startScript.canExecute() && !startScript.setExecutable(true)) {
        logger.log(Type.ERROR, "Can not execute " + JBOSS_START);
        throw new UnableToCompleteException();
      }
      ProcessBuilder builder = new ProcessBuilder(JBOSS_START, "-c", TMP_CONFIG_FILE);

      logger.log(Type.INFO, String.format("Adding JBOSS_HOME=%s to instance environment", JBOSS_HOME));
      // Necessary for JBoss AS instance to startup
      builder.environment().put("JBOSS_HOME", JBOSS_HOME);

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
      JBossServletContainerAdaptor controller = new JBossServletContainerAdaptor(port, appRootDir, DEPLOYMENT_CONTEXT,
              logger.peek(), process);
      logger.log(Type.INFO, "Controller created");
      logger.unbranch();
      return controller;
    }
    catch (UnableToCompleteException e) {
      logger.log(Type.ERROR, "Could not start servlet container controller", e);
      throw new UnableToCompleteException();
    }
  }

  private String resolveClassHidingJavaAgent( final String agent,
                                              final String hiddenPattern ) throws UnableToCompleteException {
    if (agent != null){
      if (hiddenPattern == null || hiddenPattern.isEmpty()){
          return agent;
      }
      return agent + "=classPattern=" + hiddenPattern;
    }

    logger.log(
            Type.INFO,
            "The local path to the artifact errai.org.jboss:class-local-class-hider:jar not provided. Trying to find it..." );

    final String userHome = System.getProperty( "user.home" );
    if (userHome == null) {
      logger.log(
              Type.ERROR,
              "Couldn't determine user.home directory.");
      throw new UnableToCompleteException();
    }

    final InputStream propertiesStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "META-INF/maven/org.jboss.errai/errai-cdi-jboss/pom.properties" );

    if (propertiesStream == null) {
      logger.log(
              Type.ERROR,
              "Couldn't resolve current Errai version.");
      throw new UnableToCompleteException();
    }

    final Properties properties = new Properties();
    try {
        properties.load( propertiesStream );
    } catch ( final IOException ignored ) {
    }

    final String version =  properties.getProperty( "version" );
    final String localRepo = userHome + "/.m2/repository";
    final String hider = localRepo + "/org/jboss/errai/errai-client-local-class-hider/" + version + "/errai-client-local-class-hider-" + version + ".jar";
    if (!new File(hider).exists()){
      logger.log(
              Type.ERROR,
              String.format(
                      "Couldn't resolve artifact errai.org.jboss:class-local-class-hider:jar - %s",
                      CLASS_HIDING_JAVA_AGENT_PROPERTY));
      throw new UnableToCompleteException();
    }

    if (hiddenPattern == null || hiddenPattern.isEmpty()) {
      return hider;
    }

    return hider + "=classPattern=" + hiddenPattern;
  }

  private void validateJbossHome(final String JBOSS_HOME) throws UnableToCompleteException {
    if (JBOSS_HOME == null || JBOSS_HOME.equals("")) {
      logger.log(
              Type.ERROR,
              String.format(
                      "No value for %s was given: The root directory of your Jboss installation must be provided through the property %s in your pom.xml",
                      JBOSS_HOME_PROPERTY, JBOSS_HOME_PROPERTY));
      throw new UnableToCompleteException();
    }
    
    /*
     * Check that start script and configuration folder exist.
     */
    final File[] files = new File[] {
            new File(JBOSS_HOME),
            new File(getStartScriptName(JBOSS_HOME)),
            new File(JBOSS_HOME, STANDALONE_CONFIGURATION)
    };
    
    boolean isValid = true;
    for (int i = 0; i < files.length; i++) {
      if (!files[i].exists()) {
        isValid = false;
        break;
      }
    }
    
    if (!isValid) {
      logger.branch(Type.ERROR, String.format("The errai.jboss.home directory, %s, does not appear to be home to a Jboss or Wildfly instance.", JBOSS_HOME));
      
      for (int i = 0; i < files.length; i++) {
        if (!files[i].exists()) {
          logger.log(Type.ERROR, String.format("%s not found.", files[i].getAbsolutePath()));
        }
      }
      logger.unbranch();
      
      throw new UnableToCompleteException();
    }
  }

  private void createTempConfigFile(String fromName, String toName, String jBossHome, int port) throws IOException,
          UnableToCompleteException {
    File configDir = new File(jBossHome, STANDALONE_CONFIGURATION);
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

  private String getStartScriptName(String jbossHome) {
    final String script = System.getProperty("os.name").toLowerCase().contains("windows") ? "standalone.bat"
            : "standalone.sh";

    return String.format("%s%cbin%c%s", jbossHome, File.separatorChar, File.separatorChar, script);
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
