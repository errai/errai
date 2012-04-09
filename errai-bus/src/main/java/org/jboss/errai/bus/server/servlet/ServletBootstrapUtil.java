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
import org.jboss.errai.common.metadata.ScannerSingleton;

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
    return getService(new AbstractInitConfig() {
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
    return getService(new AbstractInitConfig() {
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

  private static ErraiService getService(final InitConfig config) {
    synchronized (getServiceLock) {
      final ServletContext context = config.getServletContext();

      final String serviceLocatorClass = config.getInitOrContextParameter("service-locator");

      ErraiService service = (ErraiService) context.getAttribute(ErraiService.class.getName());
      if (null == service) {
        final ErraiServiceConfigurator configurator = new ErraiServiceConfiguratorImpl();

        String pathElement = ServletInitAttribs.WEBSOCKETS_PATH_ELEMENT
                .getInitOrContextValue(config, "in.erraiBusWebSocket");

        String webSocketsEnabled = ServletInitAttribs.WEBSOCKETS_ENABLED.getInitOrContextValue(config);
        if (webSocketsEnabled != null) {
          ErraiConfigAttribs.WEBSOCKET_SERVLET_ENABLED.set(configurator, webSocketsEnabled);
        }

        ErraiConfigAttribs.WEBSOCKET_SERVLET_CONTEXT_PATH.set(configurator,
                context.getContextPath() + "/" + pathElement);

        // Build or lookup service
        if (serviceLocatorClass != null) {
          // locate externally created service instance, i.e. CDI
          try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> aClass = loader.loadClass(serviceLocatorClass);
            ServiceLocator locator = (ServiceLocator) aClass.newInstance();
            service = locator.locateService();
          }
          catch (Exception e) {
            throw new RuntimeException("Failed to create service", e);
          }
        }
        else {
          // create a service instance manually
          service = buildService(configurator);
        }

        // store it in servlet context
        context.setAttribute(ErraiService.class.getName(), service);
      }
      return service;
    }
  }

  @SuppressWarnings({"unchecked"})
  private static ErraiService<HttpSession> buildService(final ErraiServiceConfigurator configurator) {
    return Guice.createInjector(new AbstractModule() {
      @Override
      @SuppressWarnings({"unchecked"})
      public void configure() {
        bind(ErraiService.class).to(ErraiServiceImpl.class);
        bind(ErraiServiceConfigurator.class).toInstance(configurator);
        bind(MessageBus.class).to(ServerMessageBusImpl.class);
        bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
      }
    }).getInstance(ErraiService.class);
  }

}
