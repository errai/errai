/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.servlet;

import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.jboss.errai.common.metadata.ScannerSingleton;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

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
      public String getInitParameter(final String parameter) {
        return config.getInitParameter(parameter);
      }

      @Override
      public ServletContext getServletContext() {
        return config.getServletContext();
      }

      @Override
      public String getContextParameter(final String paramater) {
        return getServletContext().getInitParameter(paramater);
      }
    });
  }

  public static ErraiService getService(final ServletConfig config) {
    return initService(new AbstractInitConfig() {
      @Override
      public String getInitParameter(final String parameter) {
        return config.getInitParameter(parameter);
      }

      @Override
      public ServletContext getServletContext() {
        return config.getServletContext();
      }

      @Override
      public String getContextParameter(final String paramater) {
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

      final String pathElement = ServletInitAttribs.WEBSOCKETS_PATH_ELEMENT
              .getInitOrContextValue(config, "in.erraiBusWS");

      final String webSocketsEnabled = ServletInitAttribs.WEBSOCKETS_ENABLED.getInitOrContextValue(config);

      if (webSocketsEnabled != null) {
        ErraiConfigAttribs.WEBSOCKET_SERVLET_ENABLED.set(configurator, webSocketsEnabled);
      }

      final String forceSecureWebsockets = ServletInitAttribs.FORCE_SECURE_WEBSOCKETS.getInitOrContextValue(config, "false");
      ErraiConfigAttribs.FORCE_SECURE_WEBSOCKET.set(configurator, forceSecureWebsockets);
      ErraiConfigAttribs.WEBSOCKET_SERVLET_CONTEXT_PATH.set(configurator, context.getContextPath() + "/" + pathElement);

      return ErraiServiceSingleton.initSingleton(configurator);
    }
  }
}
