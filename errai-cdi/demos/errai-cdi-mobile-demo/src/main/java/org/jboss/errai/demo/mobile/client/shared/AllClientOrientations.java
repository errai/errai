package org.jboss.errai.demo.mobile.client.shared;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * A collection of all the latest client orientations, periodically fired from
 * the server and observed by the clients.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Portable
public class AllClientOrientations {

  private static int nextInstanceId = 0;
  private static synchronized int nextId() {
    return nextInstanceId++;
  }

  private final List<OrientationEvent> clientOrientations;
  private final int instanceId;

  public AllClientOrientations(List<OrientationEvent> clientOrientations) {
    this.clientOrientations = clientOrientations;
    instanceId = nextId();
  }

  public AllClientOrientations(
      @MapsTo("clientOrientations") List<OrientationEvent> clientOrientations,
      @MapsTo("instanceId") int instanceId) {
    this.clientOrientations = clientOrientations;
    this.instanceId = instanceId;
  }

  public List<OrientationEvent> getClientOrientations() {
    return clientOrientations;
  }

  public int getInstanceId() {
    return instanceId;
  }
}
