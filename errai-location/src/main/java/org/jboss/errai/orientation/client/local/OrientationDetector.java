package org.jboss.errai.orientation.client.local;

import org.jboss.errai.orientation.client.shared.OrientationEvent;

import javax.enterprise.event.Event;

public abstract class OrientationDetector {

  protected Event<OrientationEvent> orientationEventSource;

  /**
   * Stops the periodic firing of CDI OrientationEvents. If this detector was
   * already in the stopped state, calling this method has no effect.
   */
  public abstract void stopFiringOrientationEvents();

  /**
   * Starts the periodic firing of CDI OrientationEvents. If this detector was
   * already in the started state, calling this method has no effect.
   */
  public abstract void startFiringOrientationEvents();

  /**
   * Fires an {@link OrientationEvent} with the given parameters. This method is
   * meant to be called by the browser-specific logic that detects the device
   * orientation.
   */
  protected void fireOrientationEvent(double x, double y, double z) {
    orientationEventSource.fire(new OrientationEvent(x, y, z));
  }

  /**
   * The provider class that creates the detector calls this method to give us a
   * means of firing the event.
   */
  public void setOrientationEventSource(Event<OrientationEvent> orientationEventSource) {
    this.orientationEventSource = orientationEventSource;
  }
}