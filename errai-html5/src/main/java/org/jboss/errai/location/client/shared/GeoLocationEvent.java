package org.jboss.errai.location.client.shared;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author edewit
 */
@Portable
public class GeoLocationEvent {
  private double latitude;
  private double longitude;
  private double accuracy;

  public GeoLocationEvent(
          @MapsTo("latitude") double latitude,
          @MapsTo("longitude") double longitude,
          @MapsTo("accuracy") double accuracy) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.accuracy = accuracy;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getAccuracy() {
    return accuracy;
  }

  @Override
  public String toString() {
    return "GeoLocationEvent [" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            ']';
  }
}
