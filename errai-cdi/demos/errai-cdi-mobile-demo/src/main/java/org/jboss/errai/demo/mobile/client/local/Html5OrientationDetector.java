package org.jboss.errai.demo.mobile.client.local;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Detects device orientation through the official HTML 5 API, periodically firing CDI
 * events with the latest orientation info.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class Html5OrientationDetector extends OrientationDetector {

  /**
   * The listener function that's currently registered to receive orientation
   * events. If null, we are not firing orientation events.
   */
  private JavaScriptObject listener;

  private void fire(double x, double y, double z) {
    fireOrientationEvent(x, y, z);
  }

  private native void startEvents() /*-{
    if (this.@org.jboss.errai.demo.mobile.client.local.Html5OrientationDetector::listener) {
      // already registered
      return;
    }

    var that = this;

    var listener = function(e) {
      var alpha = e.alpha ? e.alpha : 0;
      that.@org.jboss.errai.demo.mobile.client.local.Html5OrientationDetector::fire(DDD)(e.gamma, e.beta, alpha);
    };
    this.@org.jboss.errai.demo.mobile.client.local.Html5OrientationDetector::listener = listener;
    $wnd.addEventListener('deviceorientation', listener, false);
  }-*/;
//
//  @Override
//  public native void stopFiringOrientationEvents() /*-{
//    var listener = @org.jboss.errai.demo.mobile.client.local.Html5OrientationDetector::listener;
//    if (listener) {
//      $wnd.removeEventListener('deviceorientation', listener, false);
//      @org.jboss.errai.demo.mobile.client.local.Html5OrientationDetector::listener = null;
//    }
//  }-*/;

  /**
   * Returns true always.
   */
  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void stopFiringOrientationEvents() {
    GWT.log("Stopping orientation events!");
  }

  @Override
  public void startFiringOrientationEvents() {
    GWT.log("Starting orientation events!!!");
    startEvents();
  }
}
