package org.jboss.errai.ioc.client.container;

/**
 * A utility class for performing lifecycle operations on the bean manager. In general, you should <em>NEVER</em>
 * use this class in your application. It is used directly by the code generator to ensure the bean manager initializes
 * in a consistent state when it is reset within the same client instance.
 *
 * @author Mike Brock
 */
public class IOCBeanManagerLifecycle {
  /**
   * Resets the bean manager by cleanly destroying all beans and taking them out of service.
   */
  public void resetBeanManager() {
    if (IOCEnvironment.isAsync()) {
      IOC.getAsyncBeanManager().destroyAllBeans();
    }
    else {
      IOC.getBeanManager().destroyAllBeans();
    }
  }
}
