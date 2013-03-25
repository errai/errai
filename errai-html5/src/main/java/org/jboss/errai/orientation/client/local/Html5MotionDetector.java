package org.jboss.errai.orientation.client.local;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

import javax.enterprise.event.Event;

/**
 * Handles DeviceMotion events, such as those fired by Gecko-based browsers on laptop computers.
 * 
 * @author jfuerth
 */
public class Html5MotionDetector implements OrientationDetector {

  private Event<OrientationEvent> orientationEventSource;
  /**
   * The listener function that's currently registered to receive orientation
   * events. If null, we are not firing orientation events.
   */
  private JavaScriptObject listener;

  @Override
  public void fireOrientationEvent(double x, double y, double z) {
    orientationEventSource.fire(new OrientationEvent(y * 90.0 + 90.0, x * 90.0, z * 90.0));
  }

  private native void startEvents() /*-{
    if (this.@org.jboss.errai.orientation.client.local.Html5MotionDetector::listener) {
      // already registered
      return;
    }
    
    var that = this;

    function handleMotionEvent(event) {
      var x = event.accelerationIncludingGravity.x;
      var y = event.accelerationIncludingGravity.y;
      var z = event.accelerationIncludingGravity.z;
   
      that.@org.jboss.errai.orientation.client.local.Html5MotionDetector::fireOrientationEvent(DDD)(x, y, z);
    }
    this.@org.jboss.errai.orientation.client.local.Html5MotionDetector::listener = handleMotionEvent;
    $wnd.addEventListener("devicemotion", handleMotionEvent, true);
  }-*/;

  @Override
  public void stopFiringOrientationEvents() {
  }

  @Override
  public void startFiringOrientationEvents() {
    GWT.log("Starting motion events!!!");
    startEvents();
  }

  @Override
  public void setOrientationEventSource(Event<OrientationEvent> orientationEventSource) {
    this.orientationEventSource = orientationEventSource;
  }
}
