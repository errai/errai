package org.jboss.errai.ui.cordova.events.touch;

import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ui.cordova.events.touch.longtap.LongTapEvent;
import org.jboss.errai.ui.cordova.events.touch.longtap.LongTapHandler;
import org.jboss.errai.ui.cordova.events.touch.longtap.LongTapRecognizer;

/**
 * @author edewit@redhat.com
 */
public class GestureUtility {

  private final Widget source;

  public GestureUtility(Widget source) {
    this.source = source;
  }

  public void addLongTapHandler(LongTapHandler handler) {
    LongTapRecognizer longTapRecognizer = new LongTapRecognizer(source);
    source.addDomHandler(longTapRecognizer, TouchStartEvent.getType());
    source.addDomHandler(longTapRecognizer, TouchMoveEvent.getType());
    source.addDomHandler(longTapRecognizer, TouchEndEvent.getType());
    source.addHandler(handler, LongTapEvent.getType());
  }
}
