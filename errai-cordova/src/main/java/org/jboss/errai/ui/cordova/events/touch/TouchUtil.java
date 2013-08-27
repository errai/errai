package org.jboss.errai.ui.cordova.events.touch;

import com.google.gwt.dom.client.Touch;

/**
 * @author edewit@redhat.com
 */
public class TouchUtil {
  public static Touch cloneTouch(Touch touch) {
    return deepClone(touch);
  }

  public static native Touch deepClone(Touch touch)/*-{
      return JSON.parse(JSON.stringify(touch));
  }-*/;
}
