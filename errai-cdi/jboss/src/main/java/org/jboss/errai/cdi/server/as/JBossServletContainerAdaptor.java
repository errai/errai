package org.jboss.errai.cdi.server.as;

import java.io.File;

import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandLineException;
import org.jboss.errai.cdi.server.gwt.util.StackTreeLogger;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * Acts as a an adaptor between gwt's ServletContainer interface and a JBoss AS 7 instance.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class JBossServletContainerAdaptor extends ServletContainer {

  private final CommandContext ctx;

  private final int port;
  private final StackTreeLogger logger;
  private final File appRootDir;
  @SuppressWarnings("unused")
  private final Process jbossProcess;

  /**
   * Initialize the command context for a remote JBoss AS instance.
   * 
   * @param port
   *          The port to which the JBoss instance binds. (not yet implemented!)
   * @param appRootDir
   *          The exploded war directory to be deployed.
   * @param treeLogger
   *          For logging events from this container.
   * @throws UnableToCompleteException
   *           Thrown if this container cannot properly connect or deploy.
   */
  public JBossServletContainerAdaptor(int port, File appRootDir, TreeLogger treeLogger, Process jbossProcess) throws UnableToCompleteException {
    this.port = port;
    this.appRootDir = appRootDir;
    logger = new StackTreeLogger(treeLogger);
    this.jbossProcess = jbossProcess;

    logger.branch(Type.INFO, "Starting container initialization...");

    CommandContext ctx = null;
    try {
      try {
        logger.branch(Type.INFO, "Creating new command context...");

        ctx = CommandContextFactory.getInstance().newCommandContext();
        this.ctx = ctx;

        logger.log(Type.INFO, "Command context created");
        logger.unbranch();
      } catch (CliInitializationException e) {
        logger.branch(TreeLogger.Type.ERROR, "Could not initialize JBoss AS command context", e);
        throw new UnableToCompleteException();
      }

      try {
logger.branch(Type.INFO, "Connecting to JBoss AS...");

        ctx.handle("connect localhost:9999");

        logger.log(Type.INFO, "Connected to JBoss AS");
        logger.unbranch();
      } catch (CommandLineException e) {
        logger.branch(Type.ERROR, "Could not connect to AS", e);
        throw new UnableToCompleteException();
      }

      try {
        logger.branch(Type.INFO, String.format("Setting AS to listen for http requests on port %d...", port));

        ctx.handle(String.format(
                "/socket-binding-group=standard-sockets/socket-binding=http:write-attribute(name=port,value=%d)", port));
        ctx.handle(":reload");
        // Give the server time to reload
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          logger.log(Type.WARN, "Interrupted while waiting for JBoss AS to reload", e);
        }

        logger.log(Type.INFO, "Port change successful");
        logger.unbranch();
      } catch (CommandLineException e1) {
        logger.branch(Type.ERROR, String.format("Could not change the http port to %d", port), e1);
        throw new UnableToCompleteException();
      }

      try {
        /*
         * Need to add deployment resource to specify exploded archive
         * 
         * path : the absolute path the deployment file/directory archive : true iff the an archived
         * file, false iff an exploded archive enabled : true iff war should be automatically
         * scanned and deployed
         */
        logger.branch(Type.INFO, String.format("Adding deployment %s at %s...", getAppName(), appRootDir.getAbsolutePath()));

        ctx.handle(String.format("/deployment=%s:add(content=[{\"path\"=>\"%s\",\"archive\"=>false}], enabled=false)",
                getAppName(), appRootDir.getAbsolutePath()));

        logger.log(Type.INFO, "Deployment resource added");
        logger.unbranch();
      } catch (CommandLineException e) {
        logger.branch(Type.ERROR, String.format("Could not add deployment %s", getAppName()), e);
        throw new UnableToCompleteException();
      }

      try {
        logger.branch(Type.INFO, String.format("Deploying %s...", getAppName()));

        ctx.handle(String.format("/deployment=%s:deploy", getAppName()));

        logger.log(Type.INFO, String.format("%s deployed", getAppName()));
        logger.unbranch();
      } catch (CommandLineException e) {
        logger.branch(Type.ERROR, String.format("Could not deploy %s", getAppName()), e);
        throw new UnableToCompleteException();
      }

    } catch (UnableToCompleteException e) {
      logger.branch(Type.INFO, "Attempting to stop container...");
      stopHelper();

      throw e;
    }

  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public void refresh() throws UnableToCompleteException {
    try {
      logger.branch(Type.INFO, String.format("Redeploying %s...", getAppName()));

      ctx.handle(String.format("/deployment=%s:redeploy", getAppName()));

      logger.log(Type.INFO, String.format("%s redeployed", getAppName()));
      logger.unbranch();
    } catch (CommandLineException e) {
      logger.log(Type.ERROR, String.format("Failed to redeploy %s", getAppName()), e);
      throw new UnableToCompleteException();
    }
  }

  @Override
  public void stop() throws UnableToCompleteException {
    try {
      logger.branch(Type.INFO, String.format("Removing %s from deployments...", getAppName()));

      ctx.handle(String.format("/deployment=%s:remove", getAppName()));

      logger.log(Type.INFO, String.format("%s removed", getAppName()));
      logger.unbranch();
    } catch (CommandLineException e) {
      logger.log(Type.ERROR, "Could not shutdown AS", e);
      throw new UnableToCompleteException();
    } finally {
      stopHelper();
    }
  }

  private void stopHelper() {
    logger.branch(Type.INFO, "Attempting to stop JBoss AS instance...");
    /*
     * There is a problem with Process#destroy where it will not reliably kill the JBoss instance.
     * So instead we must try and send a shutdown signal. If that is not possible or does not work,
     * we will log it's failure, advising the user to manually kill this process.
     */
    try {
      if (ctx.getControllerHost() == null) {
        ctx.handle("connect localhost:9999");
      }
      ctx.handle(":shutdown");

      logger.log(Type.INFO, "JBoss AS instance stopped");
      logger.unbranch();
    } catch (CommandLineException e) {
      logger.log(Type.ERROR, "Could not shutdown JBoss AS instance. "
              + "Restarting this container while a JBoss AS instance is still running will cause errors.");
    }

    logger.branch(Type.INFO, "Terminating command context...");
    ctx.terminateSession();
    logger.log(Type.INFO, "Command context terminated");
    logger.unbranch();
  }

  /**
   * @return The runtime-name for the given deployment.
   */
  private String getAppName() {
    // Deployment names must end with .war
    return appRootDir.getName().endsWith(".war") ? appRootDir.getName() : appRootDir.getName() + ".war";
  }

}
