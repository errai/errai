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

package org.jboss.errai.cdi.server.as;

import java.io.File;
import java.io.IOException;

import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.errai.cdi.server.gwt.util.StackTreeLogger;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * Acts as a an adaptor between gwt's ServletContainer interface and a JBoss
 * AS/WildFly instance.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JBossServletContainerAdaptor extends ServletContainer {

  private final CommandContext ctx;

  private final int port;
  private final StackTreeLogger logger;
  private final String context;
  @SuppressWarnings("unused")
  private final Process jbossProcess;

  private String nativeControllerPath = "remote://localhost:9999";
  private String httpControllerPath = "http-remoting://localhost:9990";
  private static final int MAX_RETRIES = 9;

  /**
   * Initialize the command context for a remote JBoss AS instance.
   *
   * @param port
   *          The port to which the JBoss instance binds.
   * @param appRootDir
   *          The exploded war directory to be deployed.
   * @param context
   *          The deployment context for the app.
   * @param treeLogger
   *          For logging events from this container.
   * @throws UnableToCompleteException
   *           Thrown if this container cannot properly connect or deploy.
   */ 
  
  public JBossServletContainerAdaptor(int port, File appRootDir, String context, TreeLogger treeLogger,
          Process jbossProcess) throws UnableToCompleteException {
	  this(port,appRootDir,context,treeLogger,jbossProcess,null, null);
  }
  
  /**
   * Initialize the command context for a remote JBoss AS instance.
   *
   * @param port
   *          The port to which the JBoss instance binds.
   * @param appRootDir
   *          The exploded war directory to be deployed.
   * @param context
   *          The deployment context for the app.
   * @param treeLogger
   *          For logging events from this container.
   * @param httpRemotingAddress
   * 		  If not null, overrides HTTP_CONTROLLER_PATH property with the specified one.
   * @param nativeRemotingAddress
   * 		  If not null, overrides NATIVE_CONTROLLER_PATH property with the specified one.
   * 
   * @throws UnableToCompleteException
   *           Thrown if this container cannot properly connect or deploy.
   */
  public JBossServletContainerAdaptor(int port, File appRootDir, String context, TreeLogger treeLogger,
          Process jbossProcess,String httpRemotingAddress, String nativeRemotingAddress ) throws UnableToCompleteException {
    this.port = port;
    logger = new StackTreeLogger(treeLogger);
    this.jbossProcess = jbossProcess;
    this.context = context;
    
    logger.branch(Type.INFO, "Starting container initialization...");
    // Overrides remoting address if and only if an ovverride is passed.
    if (httpRemotingAddress != null && !httpRemotingAddress.trim().equalsIgnoreCase(httpControllerPath)) {
    	logger.branch(Type.INFO, "Changing default 'httpControllerPath' property from ["+httpControllerPath+"] to ["+httpRemotingAddress+"]");
    	httpControllerPath = httpRemotingAddress;
    }

    if (nativeRemotingAddress != null && !nativeRemotingAddress.trim().equalsIgnoreCase(nativeControllerPath)) {
    	logger.branch(Type.INFO, "Changing default 'nativeControllerPath' property from ["+nativeControllerPath+"] to ["+nativeRemotingAddress+"]");
    	nativeControllerPath = nativeRemotingAddress;
    }

    
    CommandContext ctx = null;
    try {
      // Create command context
      try {

        logger.branch(Type.INFO, "Creating new command context...");
        ctx = CommandContextFactory.getInstance().newCommandContext();
        this.ctx = ctx;

        logger.log(Type.INFO, "Command context created");
        logger.unbranch();
      }
      catch (CliInitializationException e) {
        logger.branch(TreeLogger.Type.ERROR, "Could not initialize JBoss AS command context", e);
        throw new UnableToCompleteException();
      }

      attemptCommandContextConnection(MAX_RETRIES);

      try {
        // Undeploy the app in case the container/devmode wasn't shutdown correctly which should
        // have removed the deployment (see stop method).
        removeDeployment();
        
        /*
         * Need to add deployment resource to specify exploded archive
         *
         * path : the absolute path the deployment file/directory archive : true
         * iff the an archived file, false iff an exploded archive enabled :
         * true iff war should be automatically scanned and deployed
         */
        logger.branch(Type.INFO,
                String.format("Adding deployment %s at %s...", getAppName(), appRootDir.getAbsolutePath()));

        final ModelNode operation = getAddOperation(appRootDir.getAbsolutePath());
        final ModelNode result = ctx.getModelControllerClient().execute(operation);
        if (!Operations.isSuccessfulOutcome(result)) {
          logger.log(Type.ERROR, String.format("Could not add deployment:\nInput:\n%s\nOutput:\n%s",
                  operation.toJSONString(false), result.toJSONString(false)));
          throw new UnableToCompleteException();
        }

        logger.log(Type.INFO, "Deployment resource added");
        logger.unbranch();
      }
      catch (IOException e) {
        logger.branch(Type.ERROR, String.format("Could not add deployment %s", getAppName()), e);
        throw new UnableToCompleteException();
      }

      attemptDeploy();

    }
    catch (UnableToCompleteException e) {
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
    attemptDeploymentRelatedOp(ClientConstants.DEPLOYMENT_REDEPLOY_OPERATION);
  }

  @Override
  public void stop() throws UnableToCompleteException {
    try {
      logger.branch(Type.INFO, String.format("Removing %s from deployments...", getAppName()));

      ModelNode result = removeDeployment();
      if (!Operations.isSuccessfulOutcome(result)) {
        logger.log(
                Type.ERROR,
                String.format("Could not undeploy AS:\nInput:\n%s\nOutput:\n%s", getAppName(),
                        result.toJSONString(false)));
        throw new UnableToCompleteException();
      }

      logger.log(Type.INFO, String.format("%s removed", getAppName()));
      logger.unbranch();
    }
    catch (IOException e) {
      logger.log(Type.ERROR, "Could not shutdown AS", e);
      throw new UnableToCompleteException();
    }
    finally {
      stopHelper();
    }
  }

  private ModelNode removeDeployment() throws IOException {
    final ModelNode operation = Operations.createRemoveOperation(
            new ModelNode().add(ClientConstants.DEPLOYMENT, getAppName()));
    return ctx.getModelControllerClient().execute(operation);
  }
  
  private void attemptCommandContextConnection(final int maxRetries)
          throws UnableToCompleteException {

    String[] controllers = new String[] { httpControllerPath,  nativeControllerPath  };
    
    final String[] protocols = new String[controllers.length];
    for (int i = 0; i < controllers.length; i++) {
      protocols[i] = controllers[i].split(":", 2)[0];
    }

    for (int retry = 0; retry < maxRetries; retry++) {
      for (int i = 0; i < controllers.length; i++) {
        final String controller = controllers[i];
        final String protocol = protocols[i];
        try {
          logger.branch(Type.INFO, String.format("Attempting to connect with %s protocol.", protocol));
          ctx.connectController(controller);
          logger.log(Type.INFO, "Connected to JBoss AS");

          return;
        }
        catch (CommandLineException e) {
          logger.log(
                  Type.INFO,
                  String.format("Attempt %d failed at connecting with %s protocol", retry + 1, protocol),
                  e);
        }
        finally {
          logger.unbranch();
        }
      }

      // No connection attempts have succeeded, so wait a bit before trying
      // again.
      if (retry < maxRetries) {
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException e1) {
          logger.log(Type.WARN, "Thread was interrupted while waiting for AS to reload", e1);
        }
      }
    }

    logger.log(Type.ERROR, "Could not connect to AS");
    throw new UnableToCompleteException();
  }

  private void stopHelper() {
    logger.branch(Type.INFO, "Attempting to stop JBoss AS instance...");
    /*
     * There is a problem with Process#destroy where it will not reliably kill
     * the JBoss instance. So instead we must try and send a shutdown signal. If
     * that is not possible or does not work, we will log it's failure, advising
     * the user to manually kill this process.
     */
    try {
      if (ctx.getControllerHost() == null) {
        ctx.handle("connect localhost:9999");
      }
      ctx.handle(":shutdown");

      logger.log(Type.INFO, "JBoss AS instance stopped");
      logger.unbranch();
    }
    catch (CommandLineException e) {
      logger.log(Type.ERROR, "Could not shutdown JBoss AS instance. "
              + "Restarting this container while a JBoss AS instance is still running will cause errors.");
    }

    logger.branch(Type.INFO, "Terminating command context...");
    ctx.terminateSession();
    logger.log(Type.INFO, "Command context terminated");
    logger.unbranch();
  }

  private void attemptDeploy() throws UnableToCompleteException {
    attemptDeploymentRelatedOp(ClientConstants.DEPLOYMENT_DEPLOY_OPERATION);
  }

  private void attemptDeploymentRelatedOp(final String opName) throws UnableToCompleteException {
    try {
      logger.branch(Type.INFO, String.format("Deploying %s...", getAppName()));

      final ModelNode operation = Operations.createOperation(opName,
              new ModelNode().add(ClientConstants.DEPLOYMENT, getAppName()));
      final ModelNode result = ctx.getModelControllerClient().execute(operation);

      if (!Operations.isSuccessfulOutcome(result)) {
        logger.log(
                Type.ERROR,
                String.format("Could not %s %s:\nInput:\n%s\nOutput:\n%s", opName, getAppName(),
                        operation.toJSONString(false), result.toJSONString(false)));
        throw new UnableToCompleteException();
      }

      logger.log(Type.INFO, String.format("%s %sed", getAppName(), opName));
      logger.unbranch();
    }
    catch (IOException e) {
      logger.branch(Type.ERROR, String.format("Could not %s %s", opName, getAppName()), e);
      throw new UnableToCompleteException();
    }
  }

  /**
   * @return The runtime-name for the given deployment.
   */
  private String getAppName() {
    // Deployment names must end with .war
    return context.endsWith(".war") ? context : context + ".war";
  }

  private ModelNode getAddOperation(String path) {
    final ModelNode command = Operations.createAddOperation(new ModelNode().add(ClientConstants.DEPLOYMENT,
            getAppName()));
    final ModelNode content = new ModelNode();
    final ModelNode contentObj = new ModelNode();

    // Construct content list
    contentObj.get("path").set(path);
    contentObj.get("archive").set(false);
    content.add(contentObj);

    command.get("content").set(content);
    command.get("enabled").set(false);

    return command;
  }
  
}
