package org.jboss.errai.ui.cordova.geofencing;

/**
 * GeoFencing is supported with a plugin for cordova applications only this implementation does nothing for use
 * when we use the browser.
 *
 * @author edewit@redhat.com
 */
public class GeoFencingBrowserProvider implements GeoFencingProvider {

  @Override
  public void addRegion(Region region) {
  }

  @Override
  public void removeRegion(Region region) {
  }
}
