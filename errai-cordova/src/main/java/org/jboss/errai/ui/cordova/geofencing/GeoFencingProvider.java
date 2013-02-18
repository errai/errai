package org.jboss.errai.ui.cordova.geofencing;

import org.jboss.errai.ioc.client.api.AfterInitialization;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * GeoFencingProvider provides geo fencing events dependent on the Geofencing cordova plugin
 *
 * @author edewit@redhat.com
 * @see <a href="https://github.com/phonegap/phonegap-plugins/tree/master/iOS/Geofencing">Cordova Geofencing Plugin</a>
 */
@Singleton
public class GeoFencingProvider {

  @Inject
  protected Event<GeoFencingEvent> geoFencingEventEvent;

  @AfterInitialization
  public void init() {
    addRegionListener();
  }

  private native void addRegionListener() /*-{
    var instance = this;
    $doc.addEventListener("region-update", function (event) {
      var fid = event.regionupdate.fid;
      console.log("got region update event ['" + fid + "']");
      $entry(instance.@org.jboss.errai.ui.cordova.geofencing.GeoFencingProvider::fireCdiEvent(I)(fid));
    });
  }-*/;

  public void addRegion(Region region) {
    addRegion(region.getId(), region.getLatitude(), region.getLongitude(), region.getRadius());
  }

  private native void addRegion(int id, double latitude, double longitude, int radius) /*-{
    $wnd.DGGeofencing.addRegion(
            {"fid":id, "radius":radius, "latitude":latitude, "longitude":longitude},
            function (result) {
              console.log("region add success");
            }
    );
  }-*/;

  public void removeRegion(Region region) {
    removeRegion(region.getId(), region.getLatitude(), region.getLongitude());
  }

  private native void removeRegion(int id, double latitude, double longitude) /*-{
    $wnd.DGGeofencing.removeRegion(
            {"fid":id, "latitude":latitude, "longitude":longitude},
            function () {
              console.log("region removed")
            }
    )
  }-*/;

  protected void fireCdiEvent(int regionId) {
    geoFencingEventEvent.fire(new GeoFencingEvent(regionId));
  }
}
