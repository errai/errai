package org.jboss.errai.common.client.logging.formatters;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.jboss.errai.common.client.logging.util.StringFormat;

/**
 * Emulates the behaviour of {@link SimpleFormatter}, but the format string is
 * stored as a JSNI value.
 * 
 * @see ErraiSimpleFormatter#setSimpleFormatString(String)
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiSimpleFormatter extends Formatter {

  public static final String defaultFormat = "%1$tH:%1$tM:%1$tS %4$s [%3$s] %5$s";

  protected Date date;

  public ErraiSimpleFormatter() {
    date = new Date();
  }

  @Override
  public synchronized String format(LogRecord record) {
    date.setTime(record.getMillis());
    // Get simple class name of logger
    String name = record.getLoggerName().substring(Math.max(record.getLoggerName().lastIndexOf(".") + 1, 0));
    Object thrown = (record.getThrown() != null) ? record.getThrown() : "";

    return StringFormat.format(getSimpleFormatString(defaultFormat), date, record.getLoggerName(), name, record
            .getLevel().getName(), record.getMessage(), thrown);
  }
  
  /**
   * Set the value for the format string used by all
   * {@link ErraiSimpleFormatter} instances. The provided format string is
   * called with {@link StringFormat} like so:
   * 
   * <pre>
   *    {@code StringFormat.format(formatString, date, fullLoggerName, simpleLoggerName, level, throwable)}
   * </pre>
   * 
   * The parameters are as follows:
   * <ul>
   * <li>{@code formatString}: the format string (see
   * {@link StringFormat#format(String, Object...)} for details).</li>
   * <li>{@code date}: the log date, either a {@link Long} or {@code Date}.</li>
   * <li>{@code fullLoggerName}: the full name of the logger.</li>
   * <li>{@code simpleLoggerName}: a substring of the full logger name, starting
   * after the last '.', or the full name if no '.' occurs (i.e. "a.b.c" -->
   * "c", "abc" --> "abc").</li>
   * <li>{@code level}: the level of the this log entry (i.e. TRACE, DEBUG,
   * INFO, WARN, ERROR).</li>
   * <li>{@code throwable}: the {@code throwable} accompanying this log entry,
   * or {@code null} if none provided.</li>
   * </ul>
   * The call to {@link StringFormat#format(String, Object...)} is designed to
   * mimic the call to {@link String#format(String, Object...)} in
   * {@link SimpleFormatter}, however there are some differences. Please see
   * {@link StringFormat#format(String, Object...)} for details.
   * 
   * @param format
   *          The format string for used by all {@link ErraiSimpleFormatter} instances.
   */
  public static native void setSimpleFormatString(String format) /*-{
    if (format == null) {
      $wnd.erraiSimpleFormatString = null;
    }
    else {
      $wnd.erraiSimpleFormatString = format;
    }
  }-*/;

  /**
   * @return The global format string.
   * @see ErraiSimpleFormatter#setSimpleFormatString(String)
   */
  public static native String getSimpleFormatString() /*-{
    if ($wnd.erraiSimpleFormatString === null || $wnd.erraiSimpleFormatString === undefined
      || $wnd.erraiSimpleFormatString.length === 0) {
      return ""; 
    } 
    else {
      return $wnd.erraiSimpleFormatString;
    }
  }-*/;
  
  /**
   * @param fallback A fall-back value if no format String has been set.
   * @return The global format string, or {@code fallback} if none has been set.
   */
  public static String getSimpleFormatString(String fallback) {
    String retVal = getSimpleFormatString();
    return (!retVal.equals("")) ? retVal : fallback;
  }

}
