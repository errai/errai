package org.jboss.errai.cdi.server.gwt.util;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.jboss.errai.cdi.server.gwt.JBossLauncher;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.ServletContainerLauncher;
import com.google.gwt.core.ext.TreeLogger;

public class CLI {

  /**
   * Start a JBoss AS Embedded instance, deploy a resource, and then close the instance.
   * 
   * @param Arguments
   *          should be of form: {port_number} {war_directory}
   */
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Expected args = {<port_number>, <war_directory>}. Found args = "
              + Arrays.toString(args));
    }

    int port = covertPortNum(args[0]);
    File appRootDir = getFileFromName(args[1]);
    final PrintStream stream = new PrintStream(File.createTempFile("CLI", ".log"));
    TreeLogger logger = new TreeLogger() {
      
      @Override
      public void log(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
        if (!isLoggable(type))
          return;
        stream.println(String.format("[%s] %s", type.toString().toUpperCase(), msg));
        if (caught != null) {
          caught.printStackTrace(stream);
        }
      }
      
      @Override
      public boolean isLoggable(Type type) {
        return type.equals(Type.INFO) || type.equals(Type.ERROR) || type.equals(Type.WARN);
      }
      
      @Override
      public TreeLogger branch(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
        log(type, msg, caught, helpInfo);
        return this;
      }
    };

    ServletContainerLauncher launcher = new JBossLauncher();
    
    System.out.println("Starting container...");
    ServletContainer container = launcher.start(logger, port, appRootDir);
    System.out.println("Container started");
    
    System.out.println("Refreshing deployment...");
    container.refresh();
    System.out.println("Deployment refreshed");
    
    System.out.println("Stopping container...");
    container.stop();
    
    Field f = container.getClass().getDeclaredField("jbossProcess");
    f.setAccessible(true);
    ((Process) f.get(container)).waitFor();
    
    System.out.println("Container is stopped");
  }

  private static File getFileFromName(String path) {
    return new File(path);
  }

  private static int covertPortNum(String portString) {
    int port;
    try {
      port = Integer.valueOf(portString);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(e);
    }
    
    return port;
  }

}
