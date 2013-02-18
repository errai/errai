package org.jboss.errai.ui.cordova.geofencing;

/**
 * @author edewit@redhat.com
 */
public class Region {
  private final int id;
  private double latitude;
  private double longitude;
  private int radius;

  public Region(int id) {
    this.id = id;
  }

  public Region(int id, double latitude, double longitude) {
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public Region(int id, double latitude, double longitude, int radius) {
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.radius = radius;
  }

  public int getId() {
    return id;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public int getRadius() {
    return radius;
  }

  public void setRadius(int radius) {
    this.radius = radius;
  }
}
