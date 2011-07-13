package org.jboss.errai.cdi.rebind;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.*;
import org.jboss.errai.ioc.rebind.ioc.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;

import static org.jboss.errai.ioc.rebind.Rule.before;

@IOCExtension
public class JSR299IOCExtensionConfigurator implements IOCExtensionConfigurator {
  public void configure(final ProcessingContext context, final InjectorFactory injectorFactory,
                        final ProcessorFactory procFactory) {

    procFactory.registerHandler(Produces.class, new AnnotationHandler<Produces>() {
      @Override
      public boolean handle(final InjectableInstance instance, final Produces annotation,
                            final IOCProcessingContext context) {

        switch (instance.getTaskType()) {
          case Type:
            break;
          case PrivateField:
            instance.ensureFieldExposed();
          default:
            if (!instance.getInjectionContext().isInjectable(instance.getType())) {
              return false;
            }
        }

        injectorFactory.addInjector(new Injector() {

          {
            super.qualifyingMetadata = JSR299QualifyingMetadata.createFromAnnotations(instance.getQualifiers());
          }

          @Override
          public Statement instantiateOnly(InjectionContext injectContext, InjectableInstance injectableInstance) {
            return instance.getValueStatement();
          }

          @Override
          public Statement getType(InjectionContext injectContext, InjectableInstance injectableInstance) {
            return instance.getValueStatement();
          }

          @Override
          public boolean isInjected() {
            return true;
          }

          @Override
          public boolean isSingleton() {
            return false;
          }

          @Override
          public String getVarName() {
            return null;
          }

          @Override
          public MetaClass getInjectedType() {
            switch (instance.getTaskType()) {
              case StaticMethod:
              case Method:
                return instance.getMethod().getReturnType();
              case PrivateField:
              case Field:
                return instance.getField().getType();
              default:
                return null;
            }
          }
        });

        return true;
      }
    }, Rule.after(EntryPoint.class, ApplicationScoped.class));

    procFactory.registerHandler(ApplicationScoped.class, new AnnotationHandler<ApplicationScoped>() {
      public boolean handle(InjectableInstance instance, ApplicationScoped annotation, IOCProcessingContext context) {
        InjectionContext injectionContext = injectorFactory.getInjectionContext();
        TypeInjector i = (TypeInjector) instance.getInjector();

        if (!i.isInjected()) {
          // instantiate the bean.
          i.setSingleton(true);
          i.getType(injectionContext, null);
          injectionContext.registerInjector(i);
        }
        return true;
      }
    });


  }

  public void afterInitialization(ProcessingContext context, InjectorFactory injectorFactory,
                                  ProcessorFactory procFactory) {
  }
}
