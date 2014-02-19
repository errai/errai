package org.jboss.errai.common.client.logging.handlers;

import java.util.logging.Formatter;
import java.util.logging.Level;

import org.jboss.errai.common.client.logging.formatters.ErraiSimpleFormatter;

import com.google.gwt.logging.client.ConsoleLogHandler;

/**
 * An extension of {@link ConsoleLogHandler} that uses a given {@link Formatter}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiConsoleLogHandler extends ConsoleLogHandler implements ErraiLogHandler {
  
  /*
   * Workaround to so that superlcass does not overwrite log level
   */
  private boolean init = false;

  public ErraiConsoleLogHandler(final Formatter formatter) {
    setFormatter(formatter);
    init = true;
  }
  
  public ErraiConsoleLogHandler() {
    this(new ErraiSimpleFormatter());
  }
  
  @Override
  public boolean isEnabled() {
    return !getLevel().equals(Level.OFF);
  }
  
  @Override
  public void setLevel(Level newLevel) {
    if (init)
      staticSetLevel(newLevel.getName());
  }
  
  @Override
  public Level getLevel() {
    return Level.parse(staticGetLevel());
  }
  
  public static native void staticSetLevel(String newLevel) /*-{
    $wnd.erraiConsoleLogHandlerLevel = newLevel;
  }-*/;
  
  public static native String staticGetLevel() /*-{
    if ($wnd.erraiConsoleLogHandlerLevel === undefined) {
      return "ALL";
    } else {
      return $wnd.erraiConsoleLogHandlerLevel;
    }
  }-*/;  
}
