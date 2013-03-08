package org.jboss.errai.ui.cordova.geofencing;

import com.google.gwt.core.client.GWT;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
public interface GeoFencingProvider {

  /**
   * Add a region that will be monitored to see if the user enters it.
   * @param region the region to monitor
   */
  void addRegion(Region region);

  /**
   * Remove the monitored region.
   * @param region region.
   */
  void removeRegion(Region region);


  @ApplicationScoped
  public static class GeoFencingProviderProducer {

    @Inject
    protected Event<GeoFencingEvent> geoFencingEventEvent;

    @Produces
    private GeoFencingProvider createGeoFencingProvider() {
      GeoFencingProvider geoFencingProvider = GWT.create(GeoFencingProvider.class);
      if (geoFencingProvider instanceof GeoFencingStandardProvider) {
        GeoFencingStandardProvider standardProvider = (GeoFencingStandardProvider) geoFencingProvider;
        standardProvider.setEventSource(geoFencingEventEvent);
      }
      return geoFencingProvider;
    }
  }
}
