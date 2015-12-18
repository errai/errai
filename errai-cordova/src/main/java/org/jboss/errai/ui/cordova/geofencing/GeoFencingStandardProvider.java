/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.cordova.geofencing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import org.jboss.errai.ui.cordova.JavascriptInjector;

import javax.enterprise.event.Event;

/**
 * GeoFencingProvider provides geo fencing events dependent on the Geofencing cordova plugin
 *
 * @author edewit@redhat.com
 * @see <a href="https://github.com/phonegap/phonegap-plugins/tree/master/iOS/Geofencing">Cordova Geofencing Plugin</a>
 */
public class GeoFencingStandardProvider implements GeoFencingProvider {

  private Event<GeoFencingEvent> geoFencingEventEvent;

  public GeoFencingStandardProvider() {
    initRegionListener();
    TextResource javascript = Resources.RESOURCES.javascript();
    JavascriptInjector.inject(javascript.getText());
  }

  private native void initRegionListener() /*-{
    var instance = this;
    $doc.addEventListener("region-update", function (event) {
      var fid = event.regionupdate.fid;
      console.log("got region update event ['" + fid + "']");
      $entry(instance.@org.jboss.errai.ui.cordova.geofencing.GeoFencingStandardProvider::fireCdiEvent(I)(fid));
    });
  }-*/;

  @Override
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

  @Override
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

  public void setEventSource(Event<GeoFencingEvent> eventSource) {
    this.geoFencingEventEvent = eventSource;
  }

  public static interface Resources extends ClientBundle {
    public static Resources RESOURCES = GWT.create(Resources.class);

    @Source("org/jboss/errai/ui/cordova/js/DGGeofencing.js")
    TextResource javascript();
  }
}
