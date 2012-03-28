package org.jboss.errai.ioc.client.api;

/**
 * A disposer exposes the bean managers explicit disposal functionality. An injected disposer can be used to
 * dispose of the parameterized bean type.
 *
 * @author Mike Brock
 */
public interface Disposer<T> {

  /**
   * Requests that the bean manager dispose of the specified bean instance.
   *
   * @param beanInstance the instance of the bean to be disposed.
   */
  public void dispose(T beanInstance);
}
