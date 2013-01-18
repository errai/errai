package org.jboss.errai.ui.cordova;

import com.google.gwt.user.client.Window;
import com.googlecode.gwtphonegap.client.accelerometer.*;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.orientation.client.local.OrientationDetector;
import org.jboss.errai.orientation.client.shared.Ongoing;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Detects device orientation through the PhoneGap API, periodically firing CDI
 * events with the latest orientation info.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Cordova
@Singleton
public class PhoneGapOrientationDetector extends OrientationDetector {

  @Inject
  Accelerometer accelerometer;

  @Inject @Ongoing
  Event<OrientationEvent> event;

  private AccelerometerWatcher watcher;

  @AfterInitialization
  public void init() {
    setOrientationEventSource(event);
    startFiringOrientationEvents();
  }

  @Override
  public void startFiringOrientationEvents() {
    watcher = accelerometer.watchAcceleration(new AccelerationOptions(), new AccelerationCallback() {
      @Override
      public void onSuccess(Acceleration acceleration) {
        fireOrientationEvent(acceleration.getX(), acceleration.getY(), acceleration.getZ());
      }

      @Override
      public void onFailure() {
        //TODO really handle failure like this?
        Window.alert("Accelerometer not supported");
      }
    });
  }

  @Override
  public void stopFiringOrientationEvents() {
    if (watcher != null) {
      accelerometer.clearWatch(watcher);
    }
  }


}
