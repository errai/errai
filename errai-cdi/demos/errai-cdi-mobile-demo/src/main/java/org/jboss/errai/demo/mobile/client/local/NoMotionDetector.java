package org.jboss.errai.demo.mobile.client.local;

import com.google.gwt.user.client.Window;

/**
 * Doesn't make motion events; just pops up an alert for the user when asked to start producing events.
 * 
 * @author jfuerth
 */
public class NoMotionDetector extends OrientationDetector {

  @Override
  public void stopFiringOrientationEvents() {
  }

  @Override
  public void startFiringOrientationEvents() {
    Window.alert("Sorry, this browser does not support device motion detection");
  }

  @Override
  public boolean isReady() {
    return false;
  }

}
