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
