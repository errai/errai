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

import org.jboss.errai.cdi.client.event.FunFinishEvent;
import org.jboss.errai.cdi.client.event.FunStartEvent;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Test module used by {@see EventObserverIntegrationTest}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class EventRoutingTestModule extends EventTestObserverSuperClass {
  private Runnable verifier;

  @Inject
  private Event<FunStartEvent> startEvent;

  /**
   * start the event producers on the server
   */
  public void start() {
    startEvent.fire(new FunStartEvent());
  }

  public void onFinish(@Observes FunFinishEvent event) {
    if (verifier != null) {
      verifier.run();
    }
  }

  public void setResultVerifier(Runnable verifier) {
    this.verifier = verifier;
  }

}
