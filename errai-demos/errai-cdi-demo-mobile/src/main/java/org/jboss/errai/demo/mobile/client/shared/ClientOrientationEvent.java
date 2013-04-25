package org.jboss.errai.demo.mobile.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

/**
 * @author edewit@redhat.com
 */
@Portable
public class ClientOrientationEvent extends OrientationEvent {

  private String clientId;
  private long timestamp;

  public ClientOrientationEvent(String clientId, OrientationEvent event) {
    this(clientId, System.currentTimeMillis(), event.getX(), event.getY(), event.getZ());
  }

  public ClientOrientationEvent(@MapsTo("clientId") String clientId, @MapsTo("timestamp") long timestamp, @MapsTo("x") double x,
                                @MapsTo("y") double y, @MapsTo("z") double z) {
    super(x, y, z);
    this.clientId = clientId;
    this.timestamp = timestamp;
  }

  public String getClientId() {
    return clientId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "ClientOrientationEvent{" + "clientId='" + clientId + '\'' + '}';
  }
}
