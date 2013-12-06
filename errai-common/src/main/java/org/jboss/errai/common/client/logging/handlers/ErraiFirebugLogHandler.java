package org.jboss.errai.common.client.logging.handlers;

import java.util.logging.Formatter;
import java.util.logging.Level;

import org.jboss.errai.common.client.logging.formatters.ErraiSimpleFormatter;

import com.google.gwt.logging.client.FirebugLogHandler;

/**
 * An extension of {@link FirebugLogHandler} that uses a given {@link Formatter}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiFirebugLogHandler extends FirebugLogHandler implements ErraiLogHandler {

  public ErraiFirebugLogHandler(final Formatter formatter) {
    setFormatter(formatter);
  }
  
  public ErraiFirebugLogHandler() {
    this(new ErraiSimpleFormatter());
  }
  
  @Override
  public boolean isEnabled() {
    return !getLevel().equals(Level.OFF);
  }
  
  @Override
  public void setLevel(Level newLevel) {
    staticSetLevel(newLevel.getName());
  }
  
  @Override
  public Level getLevel() {
    return Level.parse(staticGetLevel());
  }
  
  public static native void staticSetLevel(String newLevel) /*-{
    $wnd.erraiFirebugLogHandlerLevel = newLevel;
  }-*/;
  
  public static native String staticGetLevel() /*-{
    if ($wnd.erraiFirebugLogHandlerLevel === undefined) {
      return "ALL";
    } else {
      return $wnd.erraiFirebugLogHandlerLevel;
    }
  }-*/;
  
}
