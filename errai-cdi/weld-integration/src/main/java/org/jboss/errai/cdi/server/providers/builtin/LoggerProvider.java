package org.jboss.errai.cdi.server.providers.builtin;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Named;

public class LoggerProvider {

  @Produces public Logger produceLogger(InjectionPoint injectionPoint) {
    final String loggerName;
    if (injectionPoint.getQualifiers().contains(Named.class)) {
      loggerName = injectionPoint.getAnnotated().getAnnotation(Named.class).value();
    }
    else {
      loggerName = injectionPoint.getMember().getDeclaringClass().getName();
    }
    
    return LoggerFactory.getLogger(loggerName);
  }
  
}
