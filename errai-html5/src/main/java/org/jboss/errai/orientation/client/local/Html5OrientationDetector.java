package org.jboss.errai.orientation.client.local;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

import javax.enterprise.event.Event;

/**
 * Detects device orientation through the official HTML 5 API, periodically firing CDI
 * events with the latest orientation info.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class Html5OrientationDetector implements OrientationDetector {

  private Event<OrientationEvent> orientationEventSource;
  /**
   * The listener function that's currently registered to receive orientation
   * events. If null, we are not firing orientation events.
   */
  private JavaScriptObject listener;

  @Override
  public void fireOrientationEvent(double x, double y, double z) {
    orientationEventSource.fire(new OrientationEvent(x, y, z));
  }

  private native void startEvents() /*-{
    if (this.@org.jboss.errai.orientation.client.local.Html5OrientationDetector::listener) {
      // already registered
      return;
    }

    var that = this;

    var listener = function(e) {
      var alpha = e.alpha ? e.alpha : 0;
      that.@org.jboss.errai.orientation.client.local.Html5OrientationDetector::fireOrientationEvent(DDD)(e.beta, e.gamma, alpha);
    };
    this.@org.jboss.errai.orientation.client.local.Html5OrientationDetector::listener = listener;
    $wnd.addEventListener('deviceorientation', listener, false);
  }-*/;

  @Override
  public void stopFiringOrientationEvents() {
    GWT.log("Stopping orientation events!");
  }

  @Override
  public void startFiringOrientationEvents() {
    GWT.log("Starting orientation events!!!");
    startEvents();
  }

  @Override
  public void setOrientationEventSource(Event<OrientationEvent> orientationEventSource) {
    this.orientationEventSource = orientationEventSource;
  }
}
