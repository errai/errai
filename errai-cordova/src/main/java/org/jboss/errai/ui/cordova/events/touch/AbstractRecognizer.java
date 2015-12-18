package org.jboss.errai.ui.cordova.events.touch;

import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartHandler;

/**
 * @author edewit@redhat.com
 */
public abstract class AbstractRecognizer implements TouchStartHandler, TouchMoveHandler, TouchEndHandler, TouchCancelHandler {

  protected enum State {
    INVALID, READY, FINDER_DOWN, FINGERS_DOWN, WAITING, FINGERS_UP, ONE_FINGER, TWO_FINGER, FOUND_DIRECTION
  }
}
