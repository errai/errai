package org.jboss.errai.location.client.local;

import com.google.gwt.core.client.Callback;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import org.jboss.errai.location.client.shared.GeoLocationEvent;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.gwt.geolocation.client.Position.Coordinates;

/**
 * @author edewit
 */
@Singleton
public class GeoLocationProvider {

  @Inject
  protected Event<GeoLocationEvent> geoLocationEventSource;

  @PostConstruct
  public void init() {
    Geolocation geolocation = Geolocation.getIfSupported();
    if (geolocation != null) {
      geolocation.watchPosition(new Callback<Position, PositionError>() {
        @Override
        public void onFailure(PositionError reason) {
          //TODO handle error
        }

        @Override
        public void onSuccess(Position result) {
          fire(result);
        }
      });
    }
  }

  private void fire(Position result) {
    Coordinates c = result.getCoordinates();
    GeoLocationEvent geoLocationEvent = new GeoLocationEvent(c.getLatitude(), c.getLongitude(), c.getAccuracy());
    geoLocationEventSource.fire(geoLocationEvent);
  }
}
