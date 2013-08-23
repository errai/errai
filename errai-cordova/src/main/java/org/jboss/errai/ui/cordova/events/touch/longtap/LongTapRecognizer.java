package org.jboss.errai.ui.cordova.events.touch.longtap;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HasHandlers;
import org.jboss.errai.ui.cordova.events.touch.GwtTimerExecutor;
import org.jboss.errai.ui.cordova.events.touch.TimerExecutor;

/**
 * @author edewit@redhat.com
 */
public class LongTapRecognizer implements TouchStartHandler, TouchMoveHandler, TouchEndHandler {

  public static final int DEFAULT_WAIT_TIME_IN_MS = 1500;
  public static final int DEFAULT_MAX_DISTANCE = 15;

  private final HasHandlers source;
  private State state = State.READY;
  private TimerExecutor timerExecutor;

  protected enum State {
    INVALID, READY, FINGERS_DOWN, FINGERS_UP, WAITING
  }

  public LongTapRecognizer(HasHandlers source) {
    this(source, new GwtTimerExecutor());
  }

  protected LongTapRecognizer(HasHandlers source, TimerExecutor timerExecutor) {
    this.source = source;
    this.timerExecutor = timerExecutor;
  }

  @Override
  public void onTouchStart(final TouchStartEvent event) {
    switch (state) {
      case INVALID:
        break;
      case READY:
        state = State.FINGERS_DOWN;
        break;
      case FINGERS_DOWN:
        break;
      case FINGERS_UP:
      default:
        state = State.INVALID;
        break;
    }

    timerExecutor.execute(new TimerExecutor.CodeToRun() {
      @Override
      public void onExecution() {
        source.fireEvent(new LongTapEvent(source, 1, 0, event.getTouches()));
      }
    }, DEFAULT_WAIT_TIME_IN_MS);
  }

  @Override
  public void onTouchEnd(TouchEndEvent event) {
  }

  @Override
  public void onTouchMove(TouchMoveEvent event) {
  }
}
