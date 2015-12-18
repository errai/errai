package org.jboss.errai.orientation.client.shared;

import org.jboss.errai.common.client.api.annotations.MapsTo;

public class OrientationEvent {

  private final double x;
  private final double y;
  private final double z;

  public OrientationEvent(
      @MapsTo("x") double x,
      @MapsTo("y") double y,
      @MapsTo("z") double z) {
    this.x = x;
    this.y = y;
    this.z = z;
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

  @Override
  public String toString() {
    return "OrientationEvent [x=" + x + ", y=" + y + ", z=" + z + "]";
  }

}
