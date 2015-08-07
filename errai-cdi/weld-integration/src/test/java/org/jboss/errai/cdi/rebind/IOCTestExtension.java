package org.jboss.errai.cdi.rebind;

import org.jboss.errai.cdi.injection.client.Funject;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessor;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

/**
 * @author Mike Brock
 */
@IOCExtension
public class IOCTestExtension  implements IOCExtensionConfigurator {
  @Override
  public void configure(IOCProcessingContext context, InjectionContext injectionContext, IOCProcessor procFactory) {
    injectionContext.mapElementType(WiringElementType.InjectionPoint, Funject.class);
  }

  @Override
  public void afterInitialization(IOCProcessingContext context, InjectionContext injectionContext, IOCProcessor procFactory) {
  }
}
