package org.jboss.errai.ioc.client.lifecycle.api;

/**
 * Generates {@link LifecycleListener LifecycleListeners} for IOC bean instances
 * of type {@code T}. Every instance of an IOC bean type will have the method
 * {@link #newInstance()} invoked exactly once.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface LifecycleListenerGenerator<T> {

  /**
   * @return An instance of {@link LifecycleListener}.
   */
  public LifecycleListener<T> newInstance();

}
