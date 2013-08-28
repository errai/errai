package org.jboss.errai.ui.cordova.events.touch.longtap;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.ui.cordova.events.touch.TimerExecutor;
import org.jboss.errai.ui.cordova.events.touch.mock.MockHasHandlers;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchMoveEvent;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchStartEvent;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.errai.ui.cordova.events.touch.TimerExecutor.CodeToRun;
import static org.junit.Assert.*;

/**
 * @author edewit@redhat.com
 */
@RunWith(GwtMockitoTestRunner.class)
public class LongTapRecogniserTest {

  private final MockHasHandlers handlers;
  private final LongTapRecognizer longTapRecognizer;
  private CodeToRun codeToRun;

  public LongTapRecogniserTest() {
    handlers = new MockHasHandlers();
    longTapRecognizer = new LongTapRecognizer(handlers, new TimerExecutor() {
      @Override
      public void execute(CodeToRun codeToRun, int time) {
        LongTapRecogniserTest.this.codeToRun = codeToRun;
      }
    }, 1);
  }

  @Test
  public void shouldDetectLongTap() throws InterruptedException {

    // when
    longTapRecognizer.onTouchStart(new MockTouchStartEvent(1, 2, 3));
    codeToRun.onExecution();

    // then
    GwtEvent<?> event = handlers.getEvent();

    if (!(event instanceof LongTapEvent)) {
      fail("no longtap recognized");
    }
    LongTapEvent tapEvent = (LongTapEvent) event;

    assertEquals(2, tapEvent.getStartPositions().get(0).getX());
    assertEquals(3, tapEvent.getStartPositions().get(0).getY());

    assertSame(handlers, tapEvent.getSource());
  }

  @Test
  public void shouldSimpleLongTouchWithBigMoveNotFiring() {

    // when
    longTapRecognizer.onTouchStart(new MockTouchStartEvent(1, 2, 3));
    longTapRecognizer.onTouchMove(new MockTouchMoveEvent(1, 20, 50));

    //simulate wait...
    codeToRun.onExecution();

    GwtEvent<?> event = handlers.getEvent();

    assertNull(event);
  }
}
