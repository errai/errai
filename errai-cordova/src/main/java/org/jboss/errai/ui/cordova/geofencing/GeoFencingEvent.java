package org.jboss.errai.ui.cordova.geofencing;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author edewit@redhat.com
 */
@Portable
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
