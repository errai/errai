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

package org.jboss.errai.cdi.event.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.event.FunEvent;
import org.jboss.errai.cdi.client.event.FunFinishEvent;
import org.jboss.errai.cdi.client.event.FunStartEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;

@ApplicationScoped
public class CdiEventRoutingTestService {

  @Inject
  private Event<FunEvent> event;

  @Inject @A
  private Event<FunEvent> eventA;

  @Inject @B
  private Event<FunEvent> eventB;

  @Inject
  private Event<FunFinishEvent> finishEvent;

  public void start(@Observes FunStartEvent event) {
    fireAll();
  }

  public void fireAll() {
    event.fire(new FunEvent(""));
    eventA.fire(new FunEvent("A"));
    eventB.fire(new FunEvent("B"));
    finishEvent.fire(new FunFinishEvent());
  }
}
