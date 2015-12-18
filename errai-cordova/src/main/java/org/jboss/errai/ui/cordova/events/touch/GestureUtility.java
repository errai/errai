package org.jboss.errai.ui.cordova.events.touch;

import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ui.cordova.events.touch.pinch.OffsetProvider;
import org.jboss.errai.ui.cordova.events.touch.pinch.PinchEvent;
import org.jboss.errai.ui.cordova.events.touch.pinch.PinchHandler;
import org.jboss.errai.ui.cordova.events.touch.pinch.PinchRecognizer;
import org.jboss.errai.ui.cordova.events.touch.swipe.*;
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
    LongTapRecognizer recognizer = new LongTapRecognizer(source);
    registerRecognizer(recognizer);
    source.addHandler(handler, LongTapEvent.getType());
  }

  public void addSwipeStartHandler(SwipeStartHandler handler) {
    initialiseSwipeRecognizer();
    source.addHandler(handler, SwipeStartEvent.getType());
  }

  public void addSwipeMoveHandler(SwipeMoveHandler handler) {
    initialiseSwipeRecognizer();
    source.addHandler(handler, SwipeMoveEvent.getType());
  }

  public void addSwipeEndHandler(SwipeEndHandler handler) {
    initialiseSwipeRecognizer();
    source.addHandler(handler, SwipeEndEvent.getType());
  }

  public void addPinchHandler(PinchHandler handler) {
    PinchRecognizer recognizer = new PinchRecognizer(source, new OffsetProvider() {
      @Override
      public int getLeft() {
        return source.getAbsoluteLeft();
      }

      @Override
      public int getTop() {
        return source.getAbsoluteTop();
      }
    });

    registerRecognizer(recognizer);
    source.addHandler(handler, PinchEvent.getType());
  }

  private void initialiseSwipeRecognizer() {
    SwipeRecognizer recognizer = new SwipeRecognizer(source);
    registerRecognizer(recognizer);
  }

  private void registerRecognizer(AbstractRecognizer recognizer) {
    source.addDomHandler(recognizer, TouchStartEvent.getType());
    source.addDomHandler(recognizer, TouchMoveEvent.getType());
    source.addDomHandler(recognizer, TouchEndEvent.getType());
    source.addDomHandler(recognizer, TouchCancelEvent.getType());
  }
}
