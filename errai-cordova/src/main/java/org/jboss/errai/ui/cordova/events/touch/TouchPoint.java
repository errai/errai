package org.jboss.errai.ui.cordova.events.touch;

import com.google.gwt.dom.client.Touch;

/**
 * Represents the touch of the user.
 * 
 * @author edewit@redhat.com
 */
public class TouchPoint {

  private final int id;
  private final int x;
  private final int y;

  public TouchPoint(int id, int x, int y) {
    this.id = id;
    this.x = x;
    this.y = y;
  }

  public TouchPoint(Touch touch) {
    this.id = touch.getIdentifier();
    this.x = touch.getPageX();
    this.y = touch.getPageY();
  }

  public int getId() {
    return id;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
