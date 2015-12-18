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

package org.jboss.errai.cdi.event.client.test;

import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * A suite of tests that change the CDI observer lists while an event is being delivered.
 * Initially, this was a regression test for ERRAI-632.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class ObserverManipulationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventObserverTestModule";
  }

  private int eventCount;

  private class EventCounter extends AbstractCDIEventCallback<String> {
    @Override
    protected void fireEvent(String event) {
      eventCount++;
      System.out.println("EventCounter got event. Count = " + eventCount);
    }
  }

  public void testAddObserverDuringEventDelivery() throws Exception {
    CDI.subscribe(String.class.getName(), new EventCounter());
    CDI.subscribe(String.class.getName(), new EventCounter());
    CDI.subscribe(String.class.getName(), new AbstractCDIEventCallback<String>() {
      @Override
      protected void fireEvent(String event) {
        System.out.println("About to add new observer during event delivery");
        CDI.subscribe(String.class.getName(), new EventCounter());
      }
    });
    CDI.subscribe(String.class.getName(), new EventCounter());
    CDI.subscribe(String.class.getName(), new EventCounter());
    CDI.subscribe(String.class.getName(), new EventCounter());

    // before the ERRAI-632 fix, the next line was throwing ConcurrentModificationException
    CDI.fireEvent("Holey Moley!");

    assertEquals(5, eventCount);
  }
}
