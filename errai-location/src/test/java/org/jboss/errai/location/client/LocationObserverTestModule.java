package org.jboss.errai.location.client;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.orientation.client.local.OrientationDetector;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@EntryPoint
public class LocationObserverTestModule {

  private List<String> receivedEvents = new ArrayList<String>();
  private Runnable verifier;

  @Inject
  OrientationDetector orientationDetector;

  @PostConstruct
  public void init() {
    orientationDetector.startFiringOrientationEvents();
  }

  @PreDestroy
  public void runVerifier() {
    verifier.run();
  }

  public void setVerifier(Runnable verifier) {
    this.verifier = verifier;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void onEventReceived(@Observes OrientationEvent orientationEvent) {
    receivedEvents.add(orientationEvent.toString());
  }

  public List<String> getReceivedEvents() {
    return receivedEvents;
  }
}
