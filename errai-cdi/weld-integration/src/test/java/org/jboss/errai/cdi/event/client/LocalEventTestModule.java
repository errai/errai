package org.jboss.errai.cdi.event.client;

import org.jboss.errai.cdi.client.event.LocalEventA;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class LocalEventTestModule {
  @Inject private Event<LocalEventA> localEventAEvent;

  private final List<LocalEventA> capturedEvents = new ArrayList<LocalEventA>();

  private void observesLocalEventA(@Observes LocalEventA localEventA) {
    capturedEvents.add(localEventA);
  }

  public List<LocalEventA> getCapturedEvents() {
    return capturedEvents;
  }

  public void fireEvent(final String eventText) {
    localEventAEvent.fire(new LocalEventA(eventText));
  }
}
