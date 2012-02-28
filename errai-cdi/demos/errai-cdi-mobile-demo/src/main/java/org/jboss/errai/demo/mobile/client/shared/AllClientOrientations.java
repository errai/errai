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

  private final List<OrientationEvent> clientOrientations;

  public AllClientOrientations(
      @MapsTo("clientOrientations") List<OrientationEvent> clientOrientations) {
    this.clientOrientations = clientOrientations;
  }

  public List<OrientationEvent> getClientOrientations() {
    return clientOrientations;
  }
}
