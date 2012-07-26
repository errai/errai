package org.jboss.errai.cdi.event.client;

import org.jboss.errai.cdi.client.event.LocalEventA;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.ioc.client.api.qualifiers.Any;

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
  @Inject @A private Event<LocalEventA> localEventAEventQual;

  private final List<LocalEventA> capturedEvents = new ArrayList<LocalEventA>();

  private void observesLocalEventA(@Observes LocalEventA localEventA) {
    capturedEvents.add(localEventA);
  }

  private void observesLocalEventWithQuals(@Observes @A LocalEventA localEventA) {
    capturedEvents.add(localEventA);
  }

  private void observesAnyLocalEvent(@Observes @Any LocalEventA localEventA) {
    capturedEvents.add(new LocalEventA(localEventA.getMessage() + ":Any"));
  }

  public List<LocalEventA> getCapturedEvents() {
    return capturedEvents;
  }

  public void fireEvent(final String eventText) {
    localEventAEvent.fire(new LocalEventA(eventText));
  }

  public void fireQualified(final String eventText) {
     localEventAEvent.fire(new LocalEventA(eventText));
  }
}
