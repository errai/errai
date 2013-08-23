package org.jboss.errai.ui.cordova.events.touch.longtap;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.ui.cordova.events.touch.TimerExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.errai.ui.cordova.events.touch.TimerExecutor.CodeToRun;
import static org.junit.Assert.*;

/**
 * @author edewit@redhat.com
 */
@RunWith(GwtMockitoTestRunner.class)
public class LongTapRecogniserTest {

  private CodeToRun codeToRun;

  @Test
  public void shouldDetectLongTap() throws InterruptedException {
    // given
    MockHasHandlers handlers = new MockHasHandlers();
    LongTapRecognizer longTapRecognizer = new LongTapRecognizer(handlers, new TimerExecutor() {
      @Override
      public void execute(CodeToRun codeToRun, int time) {
        LongTapRecogniserTest.this.codeToRun = codeToRun;
      }
    });

    // when
    longTapRecognizer.onTouchStart(new MockTouchStartEvent(1, 2, 3));
    longTapRecognizer.onTouchEnd(new MockTouchEndEvent());
    codeToRun.onExecution();

    // then
    GwtEvent<?> event = handlers.getEvent();

    if (!(event instanceof LongTapEvent)) {
      fail("no longtap recognized");
    }
    LongTapEvent tapEvent = (LongTapEvent) event;

    assertEquals(2, tapEvent.getStartPositions().get(0).getPageX());
    assertEquals(3, tapEvent.getStartPositions().get(0).getPageY());

    assertSame(handlers, tapEvent.getSource());
  }
}
