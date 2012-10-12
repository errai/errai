package org.jboss.errai.ioc.client.container;

/**
 * @author Mike Brock
 */
public class IOCEnvironment {
  private static boolean async = false;

  public static boolean isAsync() {
    return async;
  }

  public static void setAsync(final boolean bool) {
    async = bool;
  }
}
