package org.jboss.errai.ioc.tests.extensions.client.res;

import javax.inject.Inject;

import org.jboss.errai.common.client.api.annotations.AltLogger;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.slf4j.Logger;

@EntryPoint
public class ClassWithNamedLoggerField {

  public static final String LOGGER_NAME = "a unique logger name!!!";
  
  @Inject @AltLogger(LOGGER_NAME) private Logger logger;
  
  public Logger getLogger() {
    return logger;
  }
}
