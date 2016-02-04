/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Icon;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Marker;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Size;
import org.gwtopenmaps.openlayers.client.control.ModifyFeature;
import org.gwtopenmaps.openlayers.client.control.ModifyFeatureOptions;
import org.gwtopenmaps.openlayers.client.control.OverviewMap;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.event.MapMoveEndListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.LinearRing;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.geometry.Polygon;
import org.gwtopenmaps.openlayers.client.layer.Markers;
import org.gwtopenmaps.openlayers.client.layer.OSM;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.util.JSObject;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeHandler;
import org.jboss.errai.demo.grocery.client.local.map.GoogleMapBootstrapper;
import org.jboss.errai.demo.grocery.client.local.map.LocationProvider;
import org.jboss.errai.demo.grocery.client.local.map.LocationProvider.LocationCallback;
import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ioc.client.api.LoadAsync;
import org.jboss.errai.ui.cordova.geofencing.GeoFencingProvider;
import org.jboss.errai.ui.cordova.geofencing.Region;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageHidden;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.events.place.PlaceChangeMapEvent;
import com.google.gwt.maps.client.events.place.PlaceChangeMapHandler;
import com.google.gwt.maps.client.placeslib.Autocomplete;
import com.google.gwt.maps.client.placeslib.AutocompleteOptions;
import com.google.gwt.maps.client.placeslib.AutocompleteType;
import com.google.gwt.maps.client.placeslib.PlaceGeometry;
import com.google.gwt.maps.client.placeslib.PlaceResult;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;

@Dependent
@Templated("#main")
@Page
@LoadAsync
public class StorePage extends Composite {

  private static final Projection DEFAULT_PROJECTION = new Projection(
          "EPSG:4326");
  public static final int DEFAULT_RADIUS = 25;

  @Inject
  private EntityManager em;

  @Inject
  private @DataField SimplePanel mapContainer;

  @Inject
  private @DataField Button saveButton;

  @Inject
  private @AutoBound DataBinder<Store> storeBinder;

  @Inject
  private @DataField TextBox locationSearchBox;

  @Inject
  private @Bound @DataField TextBox name;

  @Inject
  private @Bound @DataField TextBox address;

  @Inject
  private @Bound @DataField ValueListBox<Integer> radius;

  @Inject
  private @DataField SuggestBox addDepartment;

  @Inject
  private @DataField DepartmentList departmentList;

  @Inject
  private LocationProvider locationProvider;

  private @PageState("id") Long requestedStoreId;

  @Inject
  private TransitionTo<StoresPage> backToStoresPage;

  @Inject
  private GeoFencingProvider geoFencingProvider;

  private Marker marker;
  private Markers markers;
  private Vector vectorLayer;
  private ModifyFeature modifyControl;

  @PageShown
  private void setup() {

    // if a store was requested, retrieve it here (otherwise, we're editing a
    // new, blank store instance)
    if (requestedStoreId != null) {
      Store found = em.find(Store.class, requestedStoreId);
      if (found == null) {
        Window.alert("No such store: " + requestedStoreId);
        backToStoresPage.go();
      }
      storeBinder.setModel(found, StateSync.FROM_MODEL);
      radius.setValue(Integer.valueOf(DEFAULT_RADIUS));
    }

    departmentList.setItems(storeBinder.getModel().getDepartments());
    MultiWordSuggestOracle dso = (MultiWordSuggestOracle) addDepartment
            .getSuggestOracle();
    for (Department d : em.createNamedQuery("allDepartments", Department.class)
            .getResultList()) {
      dso.add(d.getName());
    }
    addDepartment.getValueBox().addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
          if (addDepartment.getText().trim().length() == 0)
            return;
          Department department = Department.resolve(em,
                  addDepartment.getText());
          if (!storeBinder.getModel().getDepartments().contains(department)) {
            storeBinder.getModel().getDepartments().add(department);
            departmentList.setItems(storeBinder.getModel().getDepartments());
          }
          addDepartment.setText("");
        }
      }
    });

    GoogleMapBootstrapper.whenReady(new Runnable() {
      @Override
      public void run() {
        MapOptions defaultMapOptions = new MapOptions();
        defaultMapOptions.setNumZoomLevels(16);
        final MapWidget mapWidget = new MapWidget("100%", "100%",
                defaultMapOptions);
        mapContainer.add(mapWidget);
        final Map map = mapWidget.getMap();

        OSM osm = OSM.Mapnik("Mapnik");
        osm.setIsBaseLayer(true);
        map.addLayer(osm);
        vectorLayer = new Vector("Fence");
        map.addLayer(vectorLayer);
        markers = new Markers("Markers");
        map.addLayer(markers);
        map.addControl(new OverviewMap());
        map.addControl(new ScaleLine());

        ModifyFeatureOptions featureOptions = new ModifyFeatureOptions();
        featureOptions.setMode(ModifyFeature.RESIZE);
        featureOptions
                .onModificationEnd(new ModifyFeature.OnModificationEndListener() {
                  @Override
                  public void onModificationEnd(VectorFeature vectorFeature) {
                    int diameter = Math.round(vectorFeature.getGeometry()
                            .getBounds().getWidth());
                    storeBinder.getModel().setRadius(diameter / 2);
                  }
                });

        storeBinder.addPropertyChangeHandler("radius",
                new PropertyChangeHandler<Integer>() {
                  @Override
                  public void onPropertyChange(
                          PropertyChangeEvent<Integer> event) {
                    reDrawGeoFence(map);
                  }
                });

        modifyControl = new ModifyFeature(vectorLayer, featureOptions);
        map.addControl(modifyControl);

        placeMarkerAtStoreLocation(map);

        map.addMapMoveEndListener(new MapMoveEndListener() {

          @Override
          public void onMapMoveEnd(MapMoveEndEvent eventObject) {
            Bounds extent = map.getExtent();
            extent.transform(DEFAULT_PROJECTION, new Projection("EPSG:900913"));

            // set up autocomplete search box for this place
            AutocompleteType[] types = new AutocompleteType[2];
            types[0] = AutocompleteType.ESTABLISHMENT;
            types[1] = AutocompleteType.GEOCODE;

            AutocompleteOptions options = AutocompleteOptions.newInstance();
            options.setTypes(types);
            LatLng sw = LatLng.newInstance(extent.getLowerLeftX(),
                    extent.getLowerLeftY());
            LatLng ne = LatLng.newInstance(extent.getUpperRightX(),
                    extent.getUpperRightY());
            options.setBounds(LatLngBounds.newInstance(sw, ne));

            final Autocomplete autoComplete = Autocomplete.newInstance(
                    locationSearchBox.getElement(), options);

            autoComplete.addPlaceChangeHandler(new PlaceChangeMapHandler() {
              @Override
              public void onEvent(PlaceChangeMapEvent event) {
                PlaceResult result = autoComplete.getPlace();
                PlaceGeometry geometry = result.getGeometry();
                LatLng center = geometry.getLocation();

                Store store = storeBinder.getModel();
                store.setName(result.getName());
                store.setAddress(result.getFormatted_Address());
                store.setLatitude(center.getLatitude());
                store.setLongitude(center.getLongitude());
                store.setRadius(DEFAULT_RADIUS);

                placeMarkerAtStoreLocation(map);
              }
            });
          }
        });
      }
    });
  }

  @PageHidden
  public void cleanup() {
    if (mapContainer.getWidget() != null) {
      mapContainer.getWidget().removeFromParent();
    }
  }

  @EventHandler("saveButton")
  private void save(ClickEvent e) {
    Store store = storeBinder.getModel();

    if (!isValidName(store.getName())) {
      return;
    }

    em.merge(store);
    em.flush();

    Region region = new Region((int) store.getId(), store.getLatitude(),
            store.getLongitude(), store.getRadius());
    geoFencingProvider.addRegion(region);

    backToStoresPage.go();
  }

  /**
   * If the store's location is set to something reasonable (that is, not 0
   * degrees north, 0 degrees east), this method centers the map on that
   * location and places a marker on it. Otherwise, this method tries to center
   * the map on the user's current location.
   *
   * @param map
   *          the map to place the marker on
   */
  private void placeMarkerAtStoreLocation(final Map map) {
    // first remove old marker, if any
    if (marker != null) {
      markers.removeMarker(marker);
      marker = null;
    }

    LatLng center = getStoreLocation();
    if (center != null) {
      Size size = new Size(25, 22);
      Pixel pixel = new Pixel(-15, -11);
      Icon icon = new Icon("img/marker.png", size, pixel);
      marker = new Marker(convertPoint(map.getProjection(), center), icon);
      markers.addMarker(marker);

      centerMap(map, center, 15);
      reDrawGeoFence(map);
    }
    else {
      locationProvider.getCurrentPosition(new LocationCallback() {
        @Override
        public void onSuccess(LatLng result) {
          centerMap(map, result, 13);
        }
      });
    }
  }

  private void reDrawGeoFence(Map map) {
    LonLat center = map.getCenter();
    center.transform(map.getProjection(),
            DEFAULT_PROJECTION.getProjectionCode());
    removeGeoFence();

    Point[] points = new Point[40];

    int angle = 0;
    for (int i = 0; i < 40; i++) {
      angle += 360 / 40;
      double radius = storeBinder.getModel().getRadius() * 1000;
      LonLat lonLat = LonLat.narrowToLonLat(destinationVincenty(center.lon(),
              center.lat(), angle, radius));
      lonLat.transform("EPSG:4326", map.getProjection());
      points[i] = new Point(lonLat.lon(), lonLat.lat());
    }
    LinearRing ring = new LinearRing(points);
    Polygon polygon = new Polygon(new LinearRing[] { ring });

    vectorLayer.addFeature(new VectorFeature(polygon));
    modifyControl.activate();
  }

  private void removeGeoFence() {
    if (vectorLayer.getFeatures() != null) {
      for (VectorFeature vectorFeature : vectorLayer.getFeatures()) {
        vectorLayer.removeFeature(vectorFeature);
      }
    }
  }

  private native JSObject destinationVincenty(double lon, double lat,
          int angle, double distance) /*-{
                                      return $wnd.OpenLayers.Util.destinationVincenty(
                                      new $wnd.OpenLayers.LonLat(lon, lat), angle, distance);
                                      }-*/;

  private void centerMap(Map map, LatLng center, int zoomLevel) {
    LonLat lonlat = convertPoint(map.getProjection(), center);
    map.setCenter(lonlat, zoomLevel);
  }

  private LonLat convertPoint(String mapProjection, LatLng center) {
    LonLat lonlat = new LonLat(center.getLongitude(), center.getLatitude());
    lonlat.transform(DEFAULT_PROJECTION.getProjectionCode(), mapProjection);
    return lonlat;
  }

  /**
   * Returns the store's geolocation if it's been set, or null if the store
   * location has not been set.
   */
  private LatLng getStoreLocation() {
    Store store = storeBinder.getModel();
    if (store.getLatitude() != 0.0 || store.getLongitude() != 0.0) {
      return LatLng.newInstance(store.getLatitude(), store.getLongitude());
    }
    else {
      return null;
    }
  }

  protected boolean isValidName(String name) {
    if (name == null)
      return false;
    else if (name.isEmpty())
      return false;
    else if (name.matches(".*\\w.*")) // if name.getText() contains at least one
                                      // word character
      return true;

    return false;
  }
}
