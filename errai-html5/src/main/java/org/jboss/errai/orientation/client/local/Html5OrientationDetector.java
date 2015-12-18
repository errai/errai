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
