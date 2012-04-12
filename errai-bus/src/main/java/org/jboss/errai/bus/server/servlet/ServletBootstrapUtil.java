package org.jboss.errai.bus.server.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * @author Mike Brock
 */
public final class ServletBootstrapUtil {
  private ServletBootstrapUtil() {
  }

  static {
    ScannerSingleton.class.getName();
  }

  public static ErraiService getService(final FilterConfig config) {
    return initService(new AbstractInitConfig() {
      @Override
      public String getInitParameter(String parameter) {
        return config.getInitParameter(parameter);
      }

      @Override
      public ServletContext getServletContext() {
        return config.getServletContext();
      }

      @Override
      public String getContextParameter(String paramater) {
        return getServletContext().getInitParameter(paramater);
      }
    });
  }

  public static ErraiService getService(final ServletConfig config) {
    return initService(new AbstractInitConfig() {
      @Override
      public String getInitParameter(String parameter) {
        return config.getInitParameter(parameter);
      }

      @Override
      public ServletContext getServletContext() {
        return config.getServletContext();
      }

      @Override
      public String getContextParameter(String paramater) {
        return getServletContext().getInitParameter(paramater);
      }
    });
  }

  private static final Object getServiceLock = new Object();

  private static ErraiService initService(final InitConfig config) {
    synchronized (getServiceLock) {
      if (ErraiServiceSingleton.isInitialized()) {
        return ErraiServiceSingleton.getService();
      }

      final ServletContext context = config.getServletContext();

      final ErraiServiceConfigurator configurator = new ErraiServiceConfiguratorImpl();

      final String autoDiscoverServices
              = ServletInitAttribs.AUTO_DISCOVER_SERVICES.getInitOrContextValue(config, "false");

      if (autoDiscoverServices != null) {
        ErraiConfigAttribs.AUTO_DISCOVER_SERVICES.set(configurator, autoDiscoverServices);
      }

      String pathElement = ServletInitAttribs.WEBSOCKETS_PATH_ELEMENT
              .getInitOrContextValue(config, "in.erraiBusWebSocket");

      String webSocketsEnabled = ServletInitAttribs.WEBSOCKETS_ENABLED.getInitOrContextValue(config);


      if (webSocketsEnabled != null) {
        ErraiConfigAttribs.WEBSOCKET_SERVLET_ENABLED.set(configurator, webSocketsEnabled);
      }

      ErraiConfigAttribs.WEBSOCKET_SERVLET_CONTEXT_PATH.set(configurator,
              context.getContextPath() + "/" + pathElement);

      return ErraiServiceSingleton.initSingleton(configurator);
    }
  }
}
