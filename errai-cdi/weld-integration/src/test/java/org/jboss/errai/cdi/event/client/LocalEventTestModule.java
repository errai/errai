package org.jboss.errai.cdi.event.client;

import org.jboss.errai.cdi.client.event.LocalEventA;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.ioc.client.api.qualifiers.Any;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
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
    capturedEvents.add(new LocalEventA(localEventA.getMessage() + ":None"));
  }

  private void observesLocalEventWithQuals(@Observes @A LocalEventA localEventA) {
    capturedEvents.add(new LocalEventA(localEventA.getMessage() + ":A"));
  }

  private void observesLocalEventWithQualsB(@Observes @A @B LocalEventA localEventA) {
    capturedEvents.add(new LocalEventA(localEventA.getMessage() + ":AB"));
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
     localEventAEventQual.fire(new LocalEventA(eventText));
  }

  public void fireQualifiedWithExtraQual(final String eventText) {
    localEventAEventQual.select(new B() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return B.class;
      }
    }).fire(new LocalEventA(eventText));
  }
}
