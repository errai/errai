package org.jboss.errai.ui.cordova.geofencing;

import org.jboss.errai.common.client.api.annotations.MapsTo;

/**
 * @author edewit@redhat.com
 */
public class GeoFencingEvent {
  private final int regionId;

  public GeoFencingEvent(@MapsTo("regionId") int regionId) {
    this.regionId = regionId;
  }

  public int getRegionId() {
    return regionId;
  }

  @Override
  public String toString() {
    return "GeoFencingEvent{" +
            "regionId=" + regionId +
            '}';
  }
}
