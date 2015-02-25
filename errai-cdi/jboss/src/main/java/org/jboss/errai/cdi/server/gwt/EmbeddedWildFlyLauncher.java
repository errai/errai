package org.jboss.errai.cdi.server.gwt;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
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
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */
public class EmbeddedWildFlyLauncher extends ServletContainerLauncher {
  private static final String CLIENT_LOCAL_CLASS_PATTERN_PROPERTY = "errai.client.local.class.pattern";
  private static final String DEFAULT_CLIENT_LOCAL_CLASS_PATTERN = ".*/client/local/.*";
  
  private static final String DEVMODE_BEANS_XML_TEMPLATE = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\">\n" + 
          "  <scan>\n" +
          "$EXCLUSIONS" +
          "  </scan>\n" + 
       "</beans>";
  
  private static final String DEVMODE_BEANS_XML_EXCLUSION_TEMPLATE = "    <exclude name = \"$CLASSNAME\" />\n";
  
  static { 
    System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
  }
  
  @Override
  public ServletContainer start(final TreeLogger treeLogger, final int port, final File appRootDir) throws BindException, Exception {
    final StackTreeLogger logger = new StackTreeLogger(treeLogger);
    try {
      System.setProperty("jboss.http.port", "" + port);
      
      final String jbossHome = JBossUtil.getJBossHome(logger);
      final StandaloneServer embeddedWildFly = EmbeddedServerFactory.create(jbossHome, null, null);
      embeddedWildFly.start();
      
      prepareBeansXml(appRootDir);
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
    for (File clientLocalClass : FileUtils.listFiles(appRootDir, new ClientLocalFileFilter(), TrueFileFilter.INSTANCE)) {
      String className = clientLocalClass.getAbsolutePath()
              .replace(classesDir.getAbsolutePath(), "")
              .replace(".class", "")
              .replace(File.separator, ".")
              .substring(1);
      exclusions.append(DEVMODE_BEANS_XML_EXCLUSION_TEMPLATE.replace("$CLASSNAME", className));
    }
    
    File beansXml = new File(webInfDir, "beans.xml");
    String beansXmlContent;
    if (!beansXml.exists()) {
      beansXmlContent = DEVMODE_BEANS_XML_TEMPLATE.replace("$EXCLUSIONS", exclusions.toString());
    }
    else {
      beansXmlContent = FileUtils.readFileToString(beansXml);
      if (beansXmlContent.contains(exclusions)) 
        return;
      
      if (beansXmlContent.contains("<scan>")) {
        beansXmlContent = beansXmlContent.replace("<scan>", "<scan>\n" + exclusions);
      }
      else {
        beansXmlContent = beansXmlContent.replace("</beans>", "  <scan>\n" + exclusions + "  </scan>\n</beans>");
      }
    }
    FileUtils.write(beansXml, beansXmlContent);
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