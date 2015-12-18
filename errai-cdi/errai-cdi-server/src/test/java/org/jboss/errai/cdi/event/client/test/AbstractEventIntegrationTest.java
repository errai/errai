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

import com.google.gwt.junit.client.TimeoutException;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.cdi.client.event.MyAbstractEvent;
import org.jboss.errai.cdi.client.event.MyAbstractEventInterface;
import org.jboss.errai.cdi.client.event.MyEventImpl;
import org.jboss.errai.cdi.client.event.MyEventInterface;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all event integration tests.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@SuppressWarnings("serial")
public abstract class AbstractEventIntegrationTest extends AbstractErraiCDITest {

  /**
   * The following table describes the events being fired (left column) and the observers expected to receive these
   * events (right column). We basically fire all combinations of qualified events using three qualifiers ( @A @B @C )
   * and test if each observer received its corresponding events, and only those events. The {} empty set is used to
   * describe an event without qualifiers.
   * <p/>
   * As per the current spec, less specific observers will be notified as well, meaning that observing a subset of an
   * event's qualifiers is sufficient for receiving the event.
   * <p/>
   * Discussions on this subject can be followed here: https://issues.jboss.org/browse/WELD-860
   * https://issues.jboss.org/browse/CDI-7 spec: http://docs.jboss.org/cdi/spec/1.0/html/events.html#d0e6742
   * <p/>
   * <pre>
   * {}     => {}
   * A      => {},A
   * B      => {},B
   * C      => {},C
   * A,B    => {},A,B,AB,(BA (redundant, but still worth testing to make sure that the sequence doesn't matter))
   * A,C    => {},A,C,AC
   * B,C    => {},B,C,BC
   * A,B,C  => {},A,B,C,AB,BA,BC,AC,ABC
   * </pre>
   * <p/>
   * Maps observers to the events they should receive during execution of the tests.
   */
  protected static final Map<String, List<String>> expectedQualifiedEvents = new HashMap<String, List<String>>() {
    {
      put("", Arrays.asList(new String[]{"", "A", "B", "C", "AB", "AC", "BC", "ABC"}));
      put("Any", Arrays.asList(new String[]{"", "A", "B", "C", "AB", "AC", "BC", "ABC"}));
      put("A", Arrays.asList(new String[]{"A", "AB", "AC", "ABC"}));
      put("B", Arrays.asList(new String[]{"B", "AB", "BC", "ABC"}));
      put("C", Arrays.asList(new String[]{"C", "AC", "BC", "ABC"}));
      put("AB", Arrays.asList(new String[]{"AB", "ABC"}));
      put("BA", Arrays.asList(new String[]{"AB", "ABC"}));
      put("AC", Arrays.asList(new String[]{"AC", "ABC"}));
      put("BC", Arrays.asList(new String[]{"BC", "ABC"}));
      put("ABC", Arrays.asList(new String[]{"ABC"}));
    }
  };

  /**
   * Verify the actual qualified events received against the expected events
   *
   * @param actualEvents
   */
  protected void verifyQualifiedEvents(Map<String, List<String>> actualEvents, boolean checkAnyQualifier) {
    if (checkAnyQualifier) {
      assertEquals("Wrong events observed for @{}", expectedQualifiedEvents.get("Any"), actualEvents.get("Any"));
    }
    // These asserts could be combined but provide nicer failure messages this way
    assertEquals("Wrong events observed for @{}", expectedQualifiedEvents.get(""), actualEvents.get(""));
    assertEquals("Wrong events observed for @A", expectedQualifiedEvents.get("A"), actualEvents.get("A"));
    assertEquals("Wrong events observed for @B", expectedQualifiedEvents.get("B"), actualEvents.get("B"));
    assertEquals("Wrong events observed for @C", expectedQualifiedEvents.get("C"), actualEvents.get("C"));
    assertEquals("Wrong events observed for @AB", expectedQualifiedEvents.get("AB"), actualEvents.get("AB"));
    assertEquals("Wrong events observed for @BA", expectedQualifiedEvents.get("AB"), actualEvents.get("BA"));
    assertEquals("Wrong events observed for @AC", expectedQualifiedEvents.get("AC"), actualEvents.get("AC"));
    assertEquals("Wrong events observed for @BC", expectedQualifiedEvents.get("BC"), actualEvents.get("BC"));
    assertEquals("Wrong events observed for @ABC", expectedQualifiedEvents.get("ABC"), actualEvents.get("ABC"));
  }

  /**
   * Verify the actual events received (observing super types) against the expected events
   *
   * @param actualEvents
   */
  protected void verifySuperTypeEvents(List<String> actualEvents) {
    // These asserts could be combined but provide nicer failure messages this way
    assertEquals("Wrong number of super type events observed", 4, actualEvents.size());

    assertTrue("Failed to observe event using its super type", actualEvents.contains(MyAbstractEvent.class.getName()));
    assertTrue("Failed to observe event using its super type's interface type",
        actualEvents.contains(MyAbstractEventInterface.class.getName()));

    assertTrue("Failed to observe event using its interface type", actualEvents.contains(MyEventInterface.class.getName()));
    assertTrue("Failed to observe event using its actual type", actualEvents.contains(MyEventImpl.class.getName()));
  }

  /**
   * The result verifier will run and finish the test after the TestModule received the FinishEvent. We use this backup
   * timer in case this event was never received. Without this timer, the test would in that case just end in a
   * {@link TimeoutException}.
   *
   * @param verifier
   */
  protected void verifyInBackupTimer(final Runnable verifier, int delayMillis) {
    Timer testResultBackupTimer = new Timer() {
      @Override
      public void run() {
        verifier.run();
      }
    };
    testResultBackupTimer.schedule(delayMillis);
  }
}
