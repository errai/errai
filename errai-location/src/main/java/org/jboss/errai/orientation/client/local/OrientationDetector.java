package org.jboss.errai.orientation.client.local;

import com.google.gwt.core.client.GWT;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public interface OrientationDetector {

  /**
   * Stops the periodic firing of CDI OrientationEvents. If this detector was
   * already in the stopped state, calling this method has no effect.
   */
  void stopFiringOrientationEvents();

  /**
   * Starts the periodic firing of CDI OrientationEvents. If this detector was
   * already in the started state, calling this method has no effect.
   */
  void startFiringOrientationEvents();

  /**
   * The provider class that creates the detector calls this method to give us a
   * means of firing the event.
   */
  void setOrientationEventSource(Event<OrientationEvent> orientationEventSource);

  void fireOrientationEvent(double x, double y, double z);

  @ApplicationScoped
  public static class OrientationDetectorProvider {

    @Inject
    private Event<OrientationEvent> orientationEventSource;

    @Produces
    public OrientationDetector createOrientationDetector() {
      OrientationDetector detector = GWT.create(OrientationDetector.class);
      detector.setOrientationEventSource(orientationEventSource);
      return detector;
    }
  }
}