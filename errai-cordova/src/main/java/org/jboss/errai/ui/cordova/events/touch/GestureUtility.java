package org.jboss.errai.ui.cordova.events.touch;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ui.cordova.events.touch.longtap.LongTapEvent;
import org.jboss.errai.ui.cordova.events.touch.longtap.LongTapHandler;

/**
 * @author edewit@redhat.com
 */
public class GestureUtility {

  private final Widget source;

  public GestureUtility(Widget source) {
    this.source = source;
  }

  public void addLongTapHandler(LongTapHandler handler) {
    source.addHandler(handler, LongTapEvent.getType());
  }
}
