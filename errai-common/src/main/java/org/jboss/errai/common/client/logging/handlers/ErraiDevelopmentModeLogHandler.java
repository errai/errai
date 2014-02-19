package org.jboss.errai.common.client.logging.handlers;

import java.util.logging.Formatter;
import java.util.logging.Level;

import org.jboss.errai.common.client.logging.formatters.ErraiSimpleFormatter;

import com.google.gwt.logging.client.DevelopmentModeLogHandler;

/**
 * An extension of {@link DevelopmentModeLogHandler} that uses a given {@link Formatter}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiDevelopmentModeLogHandler extends DevelopmentModeLogHandler implements ErraiLogHandler {
  
  /*
   * Workaround to so that superlcass does not overwrite log level
   */
  private boolean init = false;
  
  public ErraiDevelopmentModeLogHandler(final Formatter formatter) {
    setFormatter(formatter);
    init = true;
  }
  
  public ErraiDevelopmentModeLogHandler() {
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
    $wnd.erraiDevelopmentModeLogHandlerLevel = newLevel;
  }-*/;
  
  public static native String staticGetLevel() /*-{
    if ($wnd.erraiDevelopmentModeLogHandlerLevel === undefined) {
      return "ALL";
    } else {
      return $wnd.erraiDevelopmentModeLogHandlerLevel;
    }
  }-*/;
}
