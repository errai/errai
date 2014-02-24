package org.jboss.errai.ioc.client.lifecycle.api;

/**
 * A callback used by callers of
 * {@link LifecycleEvent#fireAsync(LifecycleCallback)}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface LifecycleCallback {

  /**
   * A callback used to get the result of a fired {@link LifecycleEvent}.
   * 
   * @param success
   *          True iff all {@link LifecycleListener LifecycleListeners} for this
   *          event did not {@linkplain LifecycleEvent#veto() veto}.
   */
  public void callback(boolean success);

}
