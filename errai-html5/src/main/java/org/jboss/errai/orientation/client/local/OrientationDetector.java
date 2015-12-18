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
import org.jboss.errai.orientation.client.shared.OrientationEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public interface OrientationDetector {

  /**
   * Stops the periodic firing of CDI OrientationEvents. If this detector was
   * already in the stopped state, calling this method has no effect.
   */
  void stopFiringOrientationEvents();

  /**
   * Starts the periodic firing of CDI OrientationEvents. If this detector was
   * already in the started state, calling this method has no effect.
   */
  void startFiringOrientationEvents();

  /**
   * The provider class that creates the detector calls this method to give us a
   * means of firing the event.
   */
  void setOrientationEventSource(Event<OrientationEvent> orientationEventSource);

  void fireOrientationEvent(double x, double y, double z);

  @ApplicationScoped
  public static class OrientationDetectorProvider {

    @Inject
    private Event<OrientationEvent> orientationEventSource;

    @Produces
    public OrientationDetector createOrientationDetector() {
      OrientationDetector detector = GWT.create(OrientationDetector.class);
      detector.setOrientationEventSource(orientationEventSource);
      return detector;
    }
  }
}
