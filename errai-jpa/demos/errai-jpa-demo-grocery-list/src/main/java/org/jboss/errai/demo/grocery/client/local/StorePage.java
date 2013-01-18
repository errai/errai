package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.demo.grocery.client.local.map.GoogleMapBootstrapper;
import org.jboss.errai.demo.grocery.client.shared.Store;
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

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.geolocation.client.Geolocation;
import com.google.gwt.geolocation.client.Position;
import com.google.gwt.geolocation.client.PositionError;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.place.PlaceChangeMapEvent;
import com.google.gwt.maps.client.events.place.PlaceChangeMapHandler;
import com.google.gwt.maps.client.overlays.Animation;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.maps.client.placeslib.Autocomplete;
import com.google.gwt.maps.client.placeslib.AutocompleteOptions;
import com.google.gwt.maps.client.placeslib.AutocompleteType;
import com.google.gwt.maps.client.placeslib.PlaceGeometry;
import com.google.gwt.maps.client.placeslib.PlaceResult;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

@Dependent
@Templated("#main") @Page
public class StorePage extends Composite {

  @Inject private EntityManager em;

  @Inject private @DataField SimplePanel mapContainer;
  @Inject private @DataField Button saveButton;

  @Inject private @AutoBound DataBinder<Store> storeBinder;
  @Inject private @Bound @DataField TextBox name;
  @Inject private @Bound @DataField TextBox address;
  @Inject private @DataField TextBox locationSearchBox;

  private @PageState("id") Long requestedStoreId;
  @Inject private TransitionTo<StoresPage> backToStoresPage;

  private Marker marker;

  @PageShown
  private void setup() {
    System.out.println("Setting up StorePage for store " + requestedStoreId);
    if (requestedStoreId != null) {
      Store found = em.find(Store.class, requestedStoreId);
      // TODO store might not be found. Should display error message in this case.
      if (found == null) {
        Window.alert("No such store: " + requestedStoreId);
        backToStoresPage.go();
      }
      storeBinder.setModel(found, InitialState.FROM_MODEL);
    }

    GoogleMapBootstrapper.whenReady(new Runnable() {
      @Override
      public void run() {
        System.out.println("Adding map widget to container");

        // TODO use geolocation API (fall back to google.loader.ClientLocation) to center the map
        LatLng center = LatLng.newInstance(49.496675, -102.65625);
        MapOptions opts = MapOptions.newInstance();
        opts.setZoom(4);
        opts.setCenter(center);
        opts.setMapTypeId(MapTypeId.ROADMAP);

        final MapWidget mapWidget = new MapWidget(opts);
        mapWidget.setSize("100%", "100%");
        mapContainer.add(mapWidget);
        triggerResizeEvent(mapWidget, center);
        placeMarkerAtStoreLocation(mapWidget);

        // set up autocomplete search box for this place
        AutocompleteType[] types = new AutocompleteType[2];
        types[0] = AutocompleteType.ESTABLISHMENT;
        types[1] = AutocompleteType.GEOCODE;

        AutocompleteOptions options = AutocompleteOptions.newInstance();
        options.setTypes(types);
        options.setBounds(mapWidget.getBounds());

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
   * This workaround as described at <a href=
   * "http://stackoverflow.com/questions/4528490/google-map-v3-off-center-in-hidden-div"
   * >StackOverflow</a> makes the map tiles center properly within the map div.
   * <p>
   * The center argument is required because the map has to be re-centered after
   * it has adjusted to the proper bounds (without recentering, the map's
   * pre-existing center point would lie northwest of the viewport).
   *
   * @param map the MapWidget that has just been added to the DOM. Must not be null.
   * @param center The Lat/Long coordinate that should be in the center of the map's viewport.
   */
  private native void triggerResizeEvent(MapWidget map, LatLng center) /*-{
    var mapImpl = map.@com.google.gwt.maps.client.MapWidget::getJso()();
    console.log("Sending resize to ", mapImpl);
    $wnd.google.maps.event.trigger(mapImpl, 'resize');
    map.@com.google.gwt.maps.client.MapWidget::setCenter(Lcom/google/gwt/maps/client/base/LatLng;)(center);
  }-*/;

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
      marker.close();
      marker = null;
    }

    LatLng center = getStoreLocation();
    if (center != null) {
      MarkerOptions options = MarkerOptions.newInstance();
      options.setAnimation(Animation.DROP);
      options.setPosition(center);
      marker = Marker.newInstance(options);

      map.setZoom(15);
      map.panTo(center);
      System.out.println("Panned map to " + center);

      marker.setMap(map);
    }
    else {
      Geolocation geolocation = Geolocation.getIfSupported();
      if (geolocation != null) {
        geolocation.getCurrentPosition(new Callback<Position, PositionError>() {

          @Override
          public void onSuccess(Position result) {
            map.setZoom(14);
            LatLng here = LatLng.newInstance(result.getCoordinates().getLatitude(), result.getCoordinates().getLongitude());
            map.panTo(here);
          }

          @Override
          public void onFailure(PositionError reason) {
            // fall back to Google's IP Geolocation
            map.setZoom(13);
            map.panTo(LatLng.newInstance(getGeoIpLatitude(), getGeoIpLongitude()));
          }

          private native double getGeoIpLatitude() /*-{
            return google.loader.ClientLocation.latitude;
          }-*/;

          private native double getGeoIpLongitude() /*-{
            return google.loader.ClientLocation.longitude;
          }-*/;
        });
      }
    }
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
