package org.jboss.errai.ui.cordova.geofencing;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Singleton;

/**
 * GeoFencingProvider provides geo fencing events dependent on the Geofencing cordova plugin
 *
 * @author edewit@redhat.com
 * @see <a href="https://github.com/phonegap/phonegap-plugins/tree/master/iOS/Geofencing">Cordova Geofencing Plugin</a>
 */
@Singleton
public class GeoFencingProvider {

  protected Event<GeoFencingEvent> geoFencingEventEvent;

  @PostConstruct
  public void init() {
    addRegionListener();
  }

  private native void addRegionListener() /*-{
    $wnd.document.addEventListener("region-update", function(event) {
      var fid = event.regionupdate.fid;
      var status = event.regionupdate.status;
      this.@org.jboss.errai.ui.cordova.geofencing.GeoFencingProvider::fireCdiEvent(I)(fid);
    });
  }-*/;

  public void addRegion(Region region) {
    addRegion(region.getId(), region.getLatitude(), region.getLongitude(), region.getRadius());
  }

  private native void addRegion(int id, double latitude, double longitude, int radius) /*-{
    var params = {"fid":id, "radius":radius, "latitude":latitude, "longitude":longitude};
    DGGeofencing.addRegion(
            params,
            function (result) {
              console.log("region add success");
            }
    );
  }-*/;

  public void removeRegion(Region region) {
    removeRegion(region.getId(), region.getLatitude(), region.getLongitude());
  }

  private native void removeRegion(int id, double latitude, double longitude) /*-{
    var params = {"fid":id, "latitude":latitude, "longitude":longitude};
    DGGeofencing.removeRegion(
            params,
            function () {
              console.log("region removed")
            }
    )
  }-*/;

  private void fireCdiEvent(int regionId) {
    geoFencingEventEvent.fire(new GeoFencingEvent(regionId));
  }
}
