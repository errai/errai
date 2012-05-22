package org.jboss.errai.ui.shared;

import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessorFactory;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ui.shared.api.annotations.Insert;
import org.jboss.errai.ui.shared.api.annotations.Replace;

@IOCExtension
public class IOCErraiUIConfigurator implements IOCExtensionConfigurator {

  @Override
  public void configure(IOCProcessingContext context, InjectionContext injectionContext, IOCProcessorFactory procFactory) {

    injectionContext.mapElementType(WiringElementType.InjectionPoint, Insert.class);
    injectionContext.mapElementType(WiringElementType.InjectionPoint, Replace.class);

  }

  @Override
  public void afterInitialization(IOCProcessingContext context, InjectionContext injectionContext,
          IOCProcessorFactory procFactory) {
  }

}
