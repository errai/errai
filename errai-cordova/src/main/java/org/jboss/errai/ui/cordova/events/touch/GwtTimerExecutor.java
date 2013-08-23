package org.jboss.errai.ui.cordova.events.touch;

import com.google.gwt.user.client.Timer;

/**
 * Execute code with a GWT timer
 *
 * @author Daniel Kurka
 */
public class GwtTimerExecutor implements TimerExecutor {

  private static class InternalTimer extends Timer {

    private final CodeToRun codeToRun;

    public InternalTimer(CodeToRun codeToRun) {
      this.codeToRun = codeToRun;
    }

    @Override
    public void run() {
      codeToRun.onExecution();
    }
  }

  @Override
  public void execute(final CodeToRun codeToRun, int time) {
    new InternalTimer(codeToRun).schedule(time);
  }
}
