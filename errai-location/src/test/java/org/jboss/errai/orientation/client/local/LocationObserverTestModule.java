package org.jboss.errai.orientation.client.local;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.orientation.client.local.OrientationDetector;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

/**
 * @author edewit@redhat.com
 */
@EntryPoint
public class LocationObserverTestModule {

  private final List<String> receivedEvents = new ArrayList<String>();

  @Inject
  OrientationDetector orientationDetector;

  @SuppressWarnings("UnusedDeclaration")
  public void onEventReceived(@Observes @Any OrientationEvent orientationEvent) {
    receivedEvents.add(orientationEvent.toString());
  }

  public List<String> getReceivedEvents() {
    return receivedEvents;
  }

  protected void fireMockEvent() {
    orientationDetector.fireOrientationEvent(0, 0, 0);
  }
}
