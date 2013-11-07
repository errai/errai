package org.jboss.errai.ioc.tests.extensions.client.res;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.slf4j.Logger;

@EntryPoint
public class ClassWithLoggerField {

  @Inject private Logger logger;
  
  public Logger getLogger() {
    return logger;
  }
  
}
