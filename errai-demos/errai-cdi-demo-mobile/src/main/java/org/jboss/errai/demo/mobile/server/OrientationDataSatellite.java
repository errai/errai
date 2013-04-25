package org.jboss.errai.demo.mobile.server;

import org.jboss.errai.demo.mobile.client.shared.AllClientOrientations;
import org.jboss.errai.demo.mobile.client.shared.ClientOrientationEvent;
import org.jboss.errai.demo.mobile.client.shared.Disconnected;
import org.jboss.errai.demo.mobile.client.shared.Ongoing;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Acts like a communications satellite in orbit over the attached clients:
 * receives the stream of orientation events from all attached clients,
 * aggregates them, and relays them back to the clients.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@ApplicationScoped
public class OrientationDataSatellite {

  private final Map<String, ClientOrientationEvent> clientOrientations = new ConcurrentHashMap<String, ClientOrientationEvent>();

  @Inject
  private Event<AllClientOrientations> orientationEventSrc;

  @Inject @Disconnected
  private Event<ClientOrientationEvent> disconnectEventSrc;

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  public void onClientOrientationChange(@Observes @Ongoing ClientOrientationEvent e) {
    clientOrientations.put(e.getClientId(), e);
  }

  @PostConstruct
  public void startRedistributionService() {
    executor.scheduleWithFixedDelay(new Runnable() {

      @Override
      public void run() {
        List<ClientOrientationEvent> clientOrientationList =
            new ArrayList<ClientOrientationEvent>(clientOrientations.values());
        orientationEventSrc.fire(new AllClientOrientations(clientOrientationList));

        // Notify everyone about clients who have gone away
        long cutoffTime = System.currentTimeMillis() - 2000;
        Iterator<Map.Entry<String, ClientOrientationEvent>> it = clientOrientations.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<String, ClientOrientationEvent> entry = it.next();
          if (entry.getValue().getTimestamp() < cutoffTime) {
            it.remove();
            disconnectEventSrc.fire(entry.getValue());
          }
        }
      }
    }, 1000, 250, TimeUnit.MILLISECONDS);
  }

  @PreDestroy
  public void stopRedistributionService() {
    executor.shutdown();
  }
}
