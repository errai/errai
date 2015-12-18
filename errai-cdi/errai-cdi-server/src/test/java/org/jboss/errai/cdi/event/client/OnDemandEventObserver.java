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

package org.jboss.errai.cdi.event.client;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;

import org.jboss.errai.cdi.client.event.UnobservedEvent;

/**
 * Part of the regression test for ERRAI-591.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Dependent
public class OnDemandEventObserver {

  /**
   * To protect against changes to the test suite that would create one of these
   * eagerly, thus invalidating the test that uses this observer class.
   */
  public static int instanceCount = 0;

  private static List<UnobservedEvent> eventLog = new ArrayList<UnobservedEvent>();

  public OnDemandEventObserver() {
    instanceCount++;
  }

  public void observeEvent(@Observes UnobservedEvent event) {
    eventLog.add(event);
  }

  public List<UnobservedEvent> getEventLog() {
    return eventLog;
  }
}
