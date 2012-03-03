package org.jboss.errai.demo.mobile.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

@Portable
public class OrientationEvent {

  private final String clientId;
  private final double x;
  private final double y;
  private final double z;
  private transient final long timestamp = System.currentTimeMillis();

  public OrientationEvent(
      @MapsTo("clientId") String clientId,
      @MapsTo("x") double x,
      @MapsTo("y") double y,
      @MapsTo("z") double z) {
    this.clientId = clientId;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public String getClientId() {
    return clientId;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return z;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "OrientationEvent [clientId=" + clientId + ", x=" + x + ", y=" + y + ", z=" + z + "]";
  }

}
