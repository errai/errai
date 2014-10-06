package org.jboss.errai.common.client.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.errai.common.client.logging.formatters.ErraiSimpleFormatter;
import org.jboss.errai.common.client.logging.handlers.ErraiConsoleLogHandler;
import org.jboss.errai.common.client.logging.handlers.ErraiDevelopmentModeLogHandler;
import org.jboss.errai.common.client.logging.handlers.ErraiLogHandler;
import org.jboss.errai.common.client.logging.handlers.ErraiSystemLogHandler;

import com.google.gwt.core.client.EntryPoint;

/**
 * Initializes Errai log handlers, which are:
 * <ul>
 * <li>{@link ErraiSystemLogHandler}: Prints to terminal in Dev Mode.</li>
 * <li>{@link ErraiConsoleLogHandler}: Logs to web console.</li>
 * <li>{@link EraiFirebugLogHandler}: Logs to Firefox web console.</li>
 * <li>{@link ErraiDevelopmentModeLogHandler}: Logs to Dev Mode window.</li>
 * </ul>
 * 
 * By default these handlers use the {@link ErraiSimpleFormatter} to format
 * output.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class LoggingHandlerConfigurator implements EntryPoint {

  private Map<Class<? extends ErraiLogHandler>, Handler> handlers = new HashMap<Class<? extends ErraiLogHandler>, Handler>();
  private static LoggingHandlerConfigurator instance;

  @Override
  public void onModuleLoad() {
    final Logger logger = Logger.getLogger("");

    handlers.put(ErraiSystemLogHandler.class, new ErraiSystemLogHandler());
    logger.addHandler(handlers.get(ErraiSystemLogHandler.class));

    handlers.put(ErraiConsoleLogHandler.class, new ErraiConsoleLogHandler());
    logger.addHandler(handlers.get(ErraiConsoleLogHandler.class));

    handlers.put(ErraiDevelopmentModeLogHandler.class, new ErraiDevelopmentModeLogHandler());
    logger.addHandler(handlers.get(ErraiDevelopmentModeLogHandler.class));

    instance = this;
  }

  /**
   * @return The single instance of {@link LoggingHandlerConfigurator}.
   */
  public static LoggingHandlerConfigurator get() {
    return instance;
  }

  /**
   * Get the top-level Errai log handlers. This could be useful if you wish to
   * change the {@link Level} or {@link Formatter} of a handler.
   * 
   * @param handlerType
   *          The type of an {@link ErraiLogHandler}.
   * @return The active {@link Handler} of the requested type.
   */
  @SuppressWarnings("unchecked")
  public <H extends ErraiLogHandler> H getHandler(Class<H> handlerType) {
    return (H) handlers.get(handlerType);
  }

}
