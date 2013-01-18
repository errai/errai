package org.jboss.errai.orientation.client.local;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;

/**
 * Just uses resize events to tell if the device has changed it's orientation.
 * 
 * @author jfuerth, edewit
 */
public class NoMotionDetector extends OrientationDetector {

  @Override
  public void stopFiringOrientationEvents() {
  }

  @Override
  public void startFiringOrientationEvents() {
    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        int orientation = event.getWidth() > event.getHeight() ? 90 : 0;
        fireOrientationEvent(0, orientation, 0);
      }
    });
  }
}
