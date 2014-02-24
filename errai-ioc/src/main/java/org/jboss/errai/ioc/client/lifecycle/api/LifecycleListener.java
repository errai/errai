package org.jboss.errai.ioc.client.lifecycle.api;

/**
 * A listener for observing {@link LifecycleEvent LifecycleEvents} on IOC beans.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface LifecycleListener<T> {

  /**
   * This method is called when a {@link LifecycleEvent} is called such that
   * {@link #isObserveableEventType(Class)} returns true for this listener and
   * event.
   * 
   * If {@link LifecycleEvent#veto()} is called in this method, any pending
   * listeners will be cancelled and callback of
   * {@link LifecycleEvent#fireAsync(LifecycleCallback)} will be invoked with a
   * failure result.
   * 
   * @param event
   *          The event being observed.
   */
  public void observeEvent(LifecycleEvent<T> event);

  /**
   * Check if this listener observes this event type.
   * 
   * @param eventType
   *          A type of subclass of {@link LifecycleEvent}.
   * @return True iff this listener observes this event type.
   */
  public boolean isObserveableEventType(Class<? extends LifecycleEvent<T>> eventType);

}
