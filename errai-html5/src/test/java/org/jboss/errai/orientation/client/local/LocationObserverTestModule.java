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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.orientation.client.local.OrientationDetector;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

/**
 * @author edewit@redhat.com
 */
@EntryPoint
public class LocationObserverTestModule {

  private final List<String> receivedEvents = new ArrayList<String>();

  @Inject
  OrientationDetector orientationDetector;

  @SuppressWarnings("UnusedDeclaration")
  public void onEventReceived(@Observes @Any OrientationEvent orientationEvent) {
    receivedEvents.add(orientationEvent.toString());
  }

  public List<String> getReceivedEvents() {
    return receivedEvents;
  }

  protected void fireMockEvent() {
    orientationDetector.fireOrientationEvent(0, 0, 0);
  }
}
