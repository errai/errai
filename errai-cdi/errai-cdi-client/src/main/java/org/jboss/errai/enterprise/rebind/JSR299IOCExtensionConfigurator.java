/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.enterprise.rebind;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JPackage;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.impl.gwt.GWTClass;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.AnnotationHandler;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.IOCProcessorFactory;
import org.jboss.errai.ioc.rebind.Rule;
import org.jboss.errai.ioc.rebind.ioc.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

@IOCExtension
public class JSR299IOCExtensionConfigurator implements IOCExtensionConfigurator {
  public void configure(final IOCProcessingContext context, final InjectorFactory injectorFactory,
                        final IOCProcessorFactory procFactory) {

    procFactory.registerHandler(Produces.class, new AnnotationHandler<Produces>() {
      @Override
      public boolean handle(final InjectableInstance instance, final Produces annotation,
                            final IOCProcessingContext context) {

        switch (instance.getTaskType()) {
          case Type:
            break;
          case PrivateField:
          case PrivateMethod:
            instance.ensureMemberExposed();

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
          public boolean isPseudo() {
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
              case PrivateMethod:
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
    }, Rule.after(EntryPoint.class, ApplicationScoped.class, Singleton.class));

    procFactory.registerHandler(ApplicationScoped.class, new AnnotationHandler<ApplicationScoped>() {
      public boolean handle(InjectableInstance instance, ApplicationScoped annotation, IOCProcessingContext context) {
        InjectionContext injectionContext = injectorFactory.getInjectionContext();
        TypeInjector i = (TypeInjector) instance.getInjector();

        if (!i.isInjected()) {
          i.setSingleton(true);
          i.getType(injectionContext, null);
        }
        return true;
      }
    });

    procFactory.registerHandler(Dependent.class, new AnnotationHandler<Dependent>() {
      public boolean handle(InjectableInstance instance, Dependent annotation, IOCProcessingContext context) {
        return true;
      }
    });

    if (context.getGeneratorContext() != null && context.getGeneratorContext().getTypeOracle() != null) {
      for (JPackage pkg : context.getGeneratorContext().getTypeOracle().getPackages()) {
        TypeScan: for (JClassType type : pkg.getTypes()) {
          if (type.isAbstract() || type.isInterface() != null
                  || type.getQualifiedSourceName().startsWith("java.")) continue;

          if (!type.isDefaultInstantiable()) {
            boolean hasInjectableConstructor = false;
            for (JConstructor c : type.getConstructors()) {
              if (c.isAnnotationPresent(Inject.class)) {
                hasInjectableConstructor = true;
                break;
              }
            }

            if (!hasInjectableConstructor) {
              continue;
            }
          }
          
          for (Annotation a : type.getAnnotations()) {
            Class<? extends Annotation> annoClass = a.annotationType();
            if (annoClass.isAnnotationPresent(Scope.class)
                    || annoClass.isAnnotationPresent(NormalScope.class)) {
              continue TypeScan;
            }
          }
          
          MetaClass metaClass = GWTClass.newInstance(type);

          if (injectorFactory.hasType(metaClass)) {
            continue;
          }
          
          injectorFactory.addPsuedoScopeForType(metaClass);
        }
      }
    }
  }

  public void afterInitialization(IOCProcessingContext context, InjectorFactory injectorFactory,
                                  IOCProcessorFactory procFactory) {
  }
}
