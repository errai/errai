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
import org.jboss.errai.codegen.framework.util.PrivateAccessType;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.extension.DependencyControl;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessorFactory;
import org.jboss.errai.ioc.rebind.ioc.extension.JSR330AnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.extension.Rule;
import org.jboss.errai.ioc.rebind.ioc.graph.SortUnit;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TypeDiscoveryListener;
import org.jboss.errai.ioc.rebind.ioc.injector.TypeInjector;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

@IOCExtension
public class JSR299IOCExtensionConfigurator implements IOCExtensionConfigurator {
  public void configure(final IOCProcessingContext context, final InjectionContext injectionContext,
                        final IOCProcessorFactory procFactory) {

    context.addSingletonScopeAnnotation(ApplicationScoped.class);

    procFactory.registerHandler(Produces.class, new JSR330AnnotationHandler<Produces>() {

      @Override
      public Set<SortUnit> checkDependencies(DependencyControl control, final InjectableInstance instance, Produces annotation,
                                             final IOCProcessingContext context) {

        switch (instance.getTaskType()) {
          case Type:
            break;
          case PrivateField:
          case PrivateMethod:
            instance.ensureMemberExposed(PrivateAccessType.Read);

        }

        injectionContext.registerInjector(new AbstractInjector() {
          {
            super.qualifyingMetadata = JSR299QualifyingMetadata.createFromAnnotations(instance.getQualifiers());
            this.provider = true;
            this.enclosingType = instance.getEnclosingType();

            if (injectionContext.isInjectorRegistered(enclosingType, qualifyingMetadata)) {
              setInjected(true);
            }
            else {
              context.registerTypeDiscoveryListener(new TypeDiscoveryListener() {
                @Override
                public void onDiscovery(IOCProcessingContext context, InjectionPoint injectionPoint) {
                  if (injectionPoint.getEnclosingType().equals(enclosingType)) {
                    setInjected(true);
                  }
                }
              });
            }
          }

          @Override
          public Statement getBeanInstance(InjectionContext injectContext, InjectableInstance injectableInstance) {
            return instance.getValueStatement();
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


        control.masqueradeAs(instance.getElementTypeOrMethodReturnType());
        return Collections.singleton(new SortUnit(instance.getEnclosingType(), true));
      }

      @Override
      public boolean handle(final InjectableInstance instance, final Produces annotation,
                            final IOCProcessingContext context) {

        return true;
      }

    }, Rule.before(EntryPoint.class, ApplicationScoped.class, Singleton.class));

    procFactory.registerHandler(ApplicationScoped.class, new JSR330AnnotationHandler<ApplicationScoped>() {
      public boolean handle(InjectableInstance instance, ApplicationScoped annotation, IOCProcessingContext context) {;
        TypeInjector i = (TypeInjector) instance.getInjector();
        i.setSingleton(true);

        i.getBeanInstance(injectionContext, null);
        return true;
      }
    });

    procFactory.registerHandler(Dependent.class, new JSR330AnnotationHandler<Dependent>() {
      public boolean handle(InjectableInstance instance, Dependent annotation, IOCProcessingContext context) {
        TypeInjector i = (TypeInjector) instance.getInjector();
        i.getBeanInstance(injectionContext, null);
        return true;
      }
    });

    if (context.getGeneratorContext() != null && context.getGeneratorContext().getTypeOracle() != null) {
      for (JPackage pkg : context.getGeneratorContext().getTypeOracle().getPackages()) {
        TypeScan:
        for (JClassType type : pkg.getTypes()) {
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

          MetaClass metaClass = GWTClass.newInstance(type.getOracle(), type);

          if (injectionContext.hasType(metaClass)) {
            continue;
          }

          injectionContext.addPsuedoScopeForType(metaClass);
        }
      }
    }
  }

  public void afterInitialization(IOCProcessingContext context, InjectionContext injectorFactory,
                                  IOCProcessorFactory procFactory) {
  }
}
