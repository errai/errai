/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.cordova;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.orientation.client.local.OrientationDetector;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

import com.google.gwt.user.client.Window;
import com.googlecode.gwtphonegap.client.accelerometer.Acceleration;
import com.googlecode.gwtphonegap.client.accelerometer.AccelerationCallback;
import com.googlecode.gwtphonegap.client.accelerometer.AccelerationOptions;
import com.googlecode.gwtphonegap.client.accelerometer.Accelerometer;
import com.googlecode.gwtphonegap.client.accelerometer.AccelerometerWatcher;

/**
 * Detects device orientation through the PhoneGap API, periodically firing CDI
 * events with the latest orientation info.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Cordova
@Singleton
public class PhoneGapOrientationDetector implements OrientationDetector {

  @Inject
  Accelerometer accelerometer;

  @Inject // @Ongoing
  Event<OrientationEvent> event;

  private AccelerometerWatcher watcher;
  private Event<OrientationEvent> orientationEventSource;

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

  public void fireOrientationEvent(double x, double y, double z) {
    orientationEventSource.fire(new OrientationEvent(x, y, z));
  }

  @Override
  public void setOrientationEventSource(Event<OrientationEvent> orientationEventSource) {
    this.orientationEventSource = orientationEventSource;
  }

  @Override
  public void stopFiringOrientationEvents() {
    if (watcher != null) {
      accelerometer.clearWatch(watcher);
    }
  }
}
