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
