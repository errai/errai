package org.jboss.errai.demo.grocery.client.local;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.events.place.PlaceChangeMapEvent;
import com.google.gwt.maps.client.events.place.PlaceChangeMapHandler;
import com.google.gwt.maps.client.placeslib.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.gwtopenmaps.openlayers.client.*;
import org.gwtopenmaps.openlayers.client.control.OverviewMap;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.layer.Markers;
import org.gwtopenmaps.openlayers.client.layer.OSM;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.demo.grocery.client.local.map.GoogleMapBootstrapper;
import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.nav.client.local.*;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Dependent
@Templated("#main") @Page
public class StorePage extends Composite {

  private static final Projection DEFAULT_PROJECTION = new Projection("EPSG:4326");

  @Inject private EntityManager em;

  @Inject private @DataField SimplePanel mapContainer;
  @Inject private @DataField Button saveButton;

  @Inject private @AutoBound DataBinder<Store> storeBinder;
  @Inject private @DataField TextBox locationSearchBox;
  @Inject private @Bound @DataField TextBox name;
  @Inject private @Bound @DataField TextBox address;
  @Inject private @DataField SuggestBox addDepartment;
  @Inject private @DataField DepartmentList departmentList;

  private @PageState("id") Long requestedStoreId;
  @Inject private TransitionTo<StoresPage> backToStoresPage;

  private Marker marker;
  private Markers markers;

  @PageShown
  private void setup() {

    // if a store was requested, retrieve it here (otherwise, we're editing a new, blank store instance)
    if (requestedStoreId != null) {
      Store found = em.find(Store.class, requestedStoreId);
      if (found == null) {
        Window.alert("No such store: " + requestedStoreId);
        backToStoresPage.go();
      }
      storeBinder.setModel(found, InitialState.FROM_MODEL);
    }

    departmentList.setItems(storeBinder.getModel().getDepartments());
    MultiWordSuggestOracle dso = (MultiWordSuggestOracle) addDepartment.getSuggestOracle();
    for (Department d : em.createNamedQuery("allDepartments", Department.class).getResultList()) {
      dso.add(d.getName());
    }
    addDepartment.getTextBox().addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
          if (addDepartment.getText().trim().length() == 0) return;
          Department department = Department.resolve(em, addDepartment.getText());
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
        final MapWidget mapWidget = new MapWidget("100%", "100%", defaultMapOptions);
        mapContainer.add(mapWidget);
        Map map = mapWidget.getMap();

        OSM osm = OSM.Mapnik("Mapnik");
        osm.setIsBaseLayer(true);
        map.addLayer(osm);
        markers = new Markers("Markers");
        map.addLayer(markers);
        map.addControl(new OverviewMap());
        map.addControl(new ScaleLine());

        placeMarkerAtStoreLocation(mapWidget);

        // set up autocomplete search box for this place
        AutocompleteType[] types = new AutocompleteType[2];
        types[0] = AutocompleteType.ESTABLISHMENT;
        types[1] = AutocompleteType.GEOCODE;

        Bounds extent = map.getExtent();
        extent.transform(DEFAULT_PROJECTION, new Projection("EPSG:900913"));

        AutocompleteOptions options = AutocompleteOptions.newInstance();
        options.setTypes(types);
        LatLng sw = LatLng.newInstance(extent.getLowerLeftX(), extent.getLowerLeftY());
        LatLng ne = LatLng.newInstance(extent.getUpperRightX(), extent.getUpperRightY());
        options.setBounds(LatLngBounds.newInstance(sw, ne));

        final Autocomplete autoComplete = Autocomplete.newInstance(locationSearchBox.getElement(), options);

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

            placeMarkerAtStoreLocation(mapWidget);
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
    em.persist(storeBinder.getModel());
    em.flush();
    backToStoresPage.go();
  }

  /**
   * If the store's location is set to something reasonable (that is, not 0
   * degrees north, 0 degrees east), this method centers the map on that
   * location and places a marker on it. Otherwise, this method tries to center
   * the map on the user's current location.
   *
   * @param map the map to place the marker on
   */
  private void placeMarkerAtStoreLocation(final MapWidget map) {
    // first remove old marker, if any
    if (marker != null) {
      markers.removeMarker(marker);
      marker = null;
    }

    LatLng center = getStoreLocation();
    if (center != null) {
      Size size = new Size(25, 22);
      Icon icon = new Icon("img/marker.png", size);
      marker = new Marker(convertPoint(map.getMap().getProjection(), center), icon);
      markers.addMarker(marker);

      centerMap(map.getMap(), center, 15);
    }
    else {
      Geolocation geolocation = Geolocation.getIfSupported();
      if (geolocation != null) {
        geolocation.getCurrentPosition(new Callback<Position, PositionError>() {

          @Override
          public void onSuccess(Position result) {
            LatLng here = LatLng.newInstance(result.getCoordinates().getLatitude(), result.getCoordinates().getLongitude());
            centerMap(map.getMap(), here, 14);
          }

          @Override
          public void onFailure(PositionError reason) {
            // fall back to Google's IP Geolocation
            centerMap(map.getMap(), getIpBasedLocation(), 13);
          }
        });
      }
    }
  }

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
   * Returns Google's guess at the user's physical location based on their IP address.
   */
  private static LatLng getIpBasedLocation() {
    return LatLng.newInstance(
            AjaxLoader.getClientLocation().getLatitude(),
            AjaxLoader.getClientLocation().getLongitude());
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
}
