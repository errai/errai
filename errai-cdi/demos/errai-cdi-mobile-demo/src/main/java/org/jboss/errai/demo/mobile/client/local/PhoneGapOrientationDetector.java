package org.jboss.errai.demo.mobile.client.local;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Detects device orientation through the PhoneGap API, periodically firing CDI
 * events with the latest orientation info.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class PhoneGapOrientationDetector extends OrientationDetector {

  /**
   * Handle on the Phonegap task that watches the acceleration change. If null,
   * we are not firing orientation events.
   */
  private JavaScriptObject watchID;

  @Override
  public native void startFiringOrientationEvents() /*-{

  var options = { frequency: 100 };

  this.@org.jboss.errai.demo.mobile.client.local.PhoneGapOrientationDetector::watchID = $wnd.navigator.accelerometer.watchAcceleration(
    function(acceleration) {
      this.@org.jboss.errai.demo.mobile.client.local.PhoneGapOrientationDetector::fireOrientationEvent(DDD)(
          acceleration.x, acceleration.y, acceleration.z);
    },
    function() {
      $wnd.alert("Accelerometer not supported");
    }, options);
  }-*/;

  @Override
  public native void stopFiringOrientationEvents() /*-{
    if (this.@org.jboss.errai.demo.mobile.client.local.PhoneGapOrientationDetector::watchID) {
      $wnd.navigator.accelerometer.clearWatch(this.@org.jboss.errai.demo.mobile.client.local.PhoneGapOrientationDetector::watchID);
      this.@org.jboss.errai.demo.mobile.client.local.PhoneGapOrientationDetector::watchID = null;
    }
  }-*/;

  /**
   * Returns true if Phonegap has finished initializing in the current page.
   */
  @Override
  public native boolean isReady() /*-{
    return $wnd.PhoneGap.available;
  }-*/;

}
