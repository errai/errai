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

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

import javax.enterprise.event.Event;

/**
 * Just uses resize events to tell if the device has changed it's orientation.
 * 
 * @author jfuerth, edewit
 */
public class NoMotionDetector implements OrientationDetector {

  private Event<OrientationEvent> orientationEventSource;

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

  @Override
  public void fireOrientationEvent(double x, double y, double z) {
    orientationEventSource.fire(new OrientationEvent(x, y, z));
  }

  @Override
  public void setOrientationEventSource(Event<OrientationEvent> orientationEventSource) {
    this.orientationEventSource = orientationEventSource;
  }
}
