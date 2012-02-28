package org.jboss.errai.demo.mobile.client.local;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Detects device orientation through the official HTML 5 API, periodically firing CDI
 * events with the latest orientation info.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class MozillaOrientationDetector extends OrientationDetector {

  /**
   * The listener function that's currently registered to receive orientation
   * events. If null, we are not firing orientation events.
   */
  private JavaScriptObject listener;

  @Override
  public native void startFiringOrientationEvents() /*-{
    if (this.@org.jboss.errai.demo.mobile.client.local.MozillaOrientationDetector::listener) {
      // already registered
      return;
    }
    var listener = function(e) {
      this.@org.jboss.errai.demo.mobile.client.local.MozillaOrientationDetector::fireOrientationEvent(DDDD)(
          e.x * 90.0, e.y * -90.0, 0, new Date().milliseconds);
    };
    this.@org.jboss.errai.demo.mobile.client.local.MozillaOrientationDetector::listener = listener;
    $wnd.addEventListener('MozOrientation', listener, false);
  }-*/;

  @Override
  public native void stopFiringOrientationEvents() /*-{
    var listener = this.@org.jboss.errai.demo.mobile.client.local.MozillaOrientationDetector::listener;
    if (listener) {
      $wnd.removeEventListener('MozOrientation', listener, false);
      this.@org.jboss.errai.demo.mobile.client.local.MozillaOrientationDetector::listener = null;
    }
  }-*/;

  /**
   * Returns true always.
   */
  @Override
  public boolean isReady() {
    return true;
  }
}
