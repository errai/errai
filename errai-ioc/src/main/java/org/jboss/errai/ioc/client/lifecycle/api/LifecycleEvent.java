package org.jboss.errai.ioc.client.lifecycle.api;

/**
 * The base interface for IOC Lifecycle events. Components may fire IOC Lifecycle events to
 * broadcast events to interested listeners that do not perfectly map to one of the supported IOC
 * scopes.
 * 
 * The general usage for firing an event is:
 * 
 * <pre>
 * Access<String> event = IOC.getBeanManager().lookup(Creation.class).getInstance();
 * // Need to set an instance for the event.
 * event.setInstance("String Instance!");
 * 
 * event.fireAsync(new LifecycleCallback() {
 *      {@code @Override}
 *      public void callback(boolean success) {
 *          if (success) {
 *              // Go through with the action
 *          }
 *      }
 * });
 * </pre>
 * 
 * {@link LifecycleListener LifecycleListeners} can {@linkplain #getInstance() access the event
 * instance} or {@linkplain #veto() veto the event}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface LifecycleEvent<T> {

  /**
   * Fire this event, notifying any listeners for this event type by calling the respective
   * {@link LifecycleListener#observeEvent(LifecycleEvent)} methods.
   * 
   * @param instance
   *          The bean instance associated with this event.
   */
  public void fireAsync(T instance);

  /**
   * Fire this event, notifying any listeners for this event type by calling the respective
   * {@link LifecycleListener#observeEvent(LifecycleEvent)} methods.
   * 
   * @param instance
   *          The bean instance associated with this event.
   * 
   * @param callback
   *          A callback for receiving the result of a fired event (whether or not any listeners
   *          {@linkplain #veto() vetoed}.
   */
  public void fireAsync(T instance, LifecycleCallback callback);

  /**
   * Veto this event. If this method is called by a {@link LifecycleListener} during the
   * {@link LifecycleListener#observeEvent(LifecycleEvent)} then:
   * <ul>
   * <li>Any pending listeners will not be invoked.</li>
   * <li>The event firer's {@link LifecycleCallback#callback(boolean)} will be invoked with the
   * parameter value {@code false}.
   */
  public void veto();

  /**
   * This method should only be called from within
   * {@link LifecycleListener#observeEvent(LifecycleEvent)}. The instance is set immediately before
   * calling listeners and unset immediately after.
   *
   * @return The instance for which this event has been fired or {@code null} if this event is not
   *         actively being fired.
   */
  public T getInstance();

}
