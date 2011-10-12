package org.jboss.errai.cdi.event.client.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

import com.google.gwt.user.client.Timer;

/**
 * Base class for all event integration tests.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@SuppressWarnings("serial")
public abstract class AbstractEventIntegrationTest extends AbstractErraiCDITest {

  /**
   * The following table describes the events being fired (left column)
   * and the observers expected to receive these events (right column).
   * We basically fire all combinations of qualified events using three
   * qualifiers ( @A @B @C ) and test if each observer received its corresponding events,
   * and only those events. The {} empty set is used to describe an event without
   * qualifiers.
   * <p/>
   * As per the current spec, less specific observers will be notified as well, meaning
   * that observing a subset of an event's qualifiers is sufficient for receiving the event.
   * <p/>
   * Discussions on this subject can be followed here:
   * https://issues.jboss.org/browse/WELD-860
   * https://issues.jboss.org/browse/CDI-7
   * spec: http://docs.jboss.org/cdi/spec/1.0/html/events.html#d0e6742
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
   */
  protected static final Map<String, List<String>> expectedEvents = new HashMap<String, List<String>>() {
    {
      put("", Arrays.asList(new String[]{"", "A", "B", "C", "AB", "AC", "BC", "ABC"}));
      put("A", Arrays.asList(new String[]{"A", "AB", "AC", "ABC"}));
      put("B", Arrays.asList(new String[]{"B", "AB", "BC", "ABC"}));
      put("C", Arrays.asList(new String[]{"C", "AC", "BC", "ABC"}));
      put("AB", Arrays.asList(new String[]{"AB", "ABC"}));
      put("AC", Arrays.asList(new String[]{"AC", "ABC"}));
      put("BC", Arrays.asList(new String[]{"BC", "ABC"}));
      put("ABC", Arrays.asList(new String[]{"ABC"}));
    }
  };

  /**
   * Verify the actual events against the expected events
   *
   * @param actualEvents
   */
  protected void verifyEvents(Map<String, List<String>> actualEvents) {
    assertEquals("Wrong events observed for @{}", expectedEvents.get(""), actualEvents.get(""));
    assertEquals("Wrong events observed for @A", expectedEvents.get("A"), actualEvents.get("A"));
    assertEquals("Wrong events observed for @B", expectedEvents.get("B"), actualEvents.get("B"));
    assertEquals("Wrong events observed for @C", expectedEvents.get("C"), actualEvents.get("C"));
    assertEquals("Wrong events observed for @AB", expectedEvents.get("AB"), actualEvents.get("AB"));
    assertEquals("Wrong events observed for @BA", expectedEvents.get("AB"), actualEvents.get("BA"));
    assertEquals("Wrong events observed for @AC", expectedEvents.get("AC"), actualEvents.get("AC"));
    assertEquals("Wrong events observed for @BC", expectedEvents.get("BC"), actualEvents.get("BC"));
    assertEquals("Wrong events observed for @ABC", expectedEvents.get("ABC"), actualEvents.get("ABC"));
  }

  /**
   * Runs the provided Runnable after the CDI client has been initialized.
   * 
   * @param r
   */
  protected void runAfterInit(final Runnable r) {
    runAfterInit(r, 1000);
  }

  protected void runAfterInit(final Runnable r, final int postInitDelay) {
    Runnable internalRunnable = new Runnable() {
      @Override
      public void run() {
        Timer internalTimer = new Timer() {
          @Override
          public void run() {
            r.run();
          }
        };
        internalTimer.schedule(postInitDelay);
      }
    };

    CDI.addPostInitTask(internalRunnable);
  }
}