package org.jboss.errai.bus.client.api;

/**
 * Do-nothing implementation of {@link BusLifecycleListener}. Convenient for
 * subclassing (instead of implementing BusLifecycleListener directly) when you
 * are only interested in one or two of the event types.
 * <p>
 * For example:
 * <pre>
 *     bus.addLifecycleListener(new BusLifecycleAdapter() {
 *     {@code @Override}
 *     public void busOnline(BusLifecycleEvent e) {
 *       // do stuff
 *     }
 *   });
 * </pre>
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BusLifecycleAdapter implements BusLifecycleListener {
  @Override
  public void busAssociating(BusLifecycleEvent e) {
  }

  @Override
  public void busDisassociating(BusLifecycleEvent e) {
  }

  @Override
  public void busOnline(BusLifecycleEvent e) {
  }

  @Override
  public void busOffline(BusLifecycleEvent e) {
  }
}
