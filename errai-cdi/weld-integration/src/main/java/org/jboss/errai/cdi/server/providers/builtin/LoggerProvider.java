package org.jboss.errai.cdi.server.providers.builtin;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.errai.common.client.api.annotations.AltLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerProvider {
  
  

  @Produces
  public Logger produceLogger(final InjectionPoint injectionPoint) {
    final String loggerName;
    if (injectionPoint.getAnnotated().isAnnotationPresent(AltLogger.class)) {
      loggerName = injectionPoint.getAnnotated().getAnnotation(AltLogger.class).value();
    }
    else {
      loggerName = injectionPoint.getMember().getDeclaringClass().getName();
    }

    return LoggerFactory.getLogger(loggerName);
  }

}
