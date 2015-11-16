package org.jboss.errai.cdi.server.providers.builtin;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.errai.common.client.api.annotations.NamedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerProvider {
  
  

  @Produces
  public Logger produceLogger(final InjectionPoint injectionPoint) {
    final String loggerName;
    if (injectionPoint.getAnnotated().isAnnotationPresent(NamedLogger.class)) {
      loggerName = injectionPoint.getAnnotated().getAnnotation(NamedLogger.class).value();
    }
    else {
      loggerName = injectionPoint.getMember().getDeclaringClass().getName();
    }

    return LoggerFactory.getLogger(loggerName);
  }

}
