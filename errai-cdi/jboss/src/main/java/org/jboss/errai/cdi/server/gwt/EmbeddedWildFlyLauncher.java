package org.jboss.errai.cdi.server.gwt;

import java.io.File;
import java.net.BindException;

import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.StandaloneServer;
import org.jboss.errai.cdi.server.gwt.util.JBossUtil;
import org.jboss.errai.cdi.server.gwt.util.StackTreeLogger;
import org.wildfly.security.manager.WildFlySecurityManager;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.ServletContainerLauncher;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * A {@link ServletContainerLauncher} controlling an embedded WildFly instance.
 * 
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */
public class EmbeddedWildFlyLauncher extends ServletContainerLauncher {

  private StackTreeLogger logger;
  
  @Override
  public ServletContainer start(final TreeLogger treeLogger, final int port, final File appRootDir) throws BindException, Exception {
    try {
      logger = new StackTreeLogger(treeLogger);
      String jbossHome = JBossUtil.getJBossHome(logger);
      
      final StandaloneServer embeddedWildFly = EmbeddedServerFactory.create(jbossHome, null, null);
      logger.log(Type.INFO, String.format("Deploying: %s to embedded WildFly", appRootDir.getAbsolutePath()));

      
      embeddedWildFly.start();
      //embeddedWildFly.deploy(appRootDir);
      
      return new ServletContainer() {

        @Override
        public int getPort() {
          return port;
        }

        @Override
        public void refresh() throws UnableToCompleteException {
          try {
            embeddedWildFly.deploy(appRootDir);
          } 
          catch (Exception e) {
            logger.log(Type.ERROR, "Failed to redeploy application", e);
            throw new UnableToCompleteException();
          }
        }

        @Override
        public void stop() throws UnableToCompleteException {
          embeddedWildFly.stop();
        }
      };
    }
    catch (UnableToCompleteException e) {
      logger.log(Type.ERROR, "Could not start servlet container controller", e);
      throw new UnableToCompleteException();
    }
  }

}
