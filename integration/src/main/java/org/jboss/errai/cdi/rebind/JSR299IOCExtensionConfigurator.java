package org.jboss.errai.cdi.rebind;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.AnnotationHandler;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ProcessorFactory;
import org.jboss.errai.ioc.rebind.ioc.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectorFactory;
import org.jboss.errai.ioc.rebind.ioc.TypeInjector;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

@IOCExtension
public class JSR299IOCExtensionConfigurator implements IOCExtensionConfigurator {
  public void configure(final ProcessingContext context, final InjectorFactory injectorFactory,
      final ProcessorFactory procFactory) {

    procFactory.registerHandler(ApplicationScoped.class, new AnnotationHandler<ApplicationScoped>() {
      public void handle(MetaClass type, ApplicationScoped annotation, IOCProcessingContext context) {
        InjectionContext injectionContext = injectorFactory.getInjectionContext();
        TypeInjector i = (TypeInjector) injectionContext.getInjector(type);

        if (!i.isInjected()) {
          // instantiate the bean.
          i.setSingleton(true);
          i.getType(injectionContext, null);
          injectionContext.registerInjector(i);
        }
      }
    });
  }

  public void afterInitialization(ProcessingContext context, InjectorFactory injectorFactory,
      ProcessorFactory procFactory) {}
}
