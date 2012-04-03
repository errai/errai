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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Scope;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.enterprise.client.cdi.CDIEventTypeLookup;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessorFactory;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JPackage;

@IOCExtension
public class JSR299IOCExtensionConfigurator implements IOCExtensionConfigurator {
  public void configure(final IOCProcessingContext context, final InjectionContext injectionContext,
                        final IOCProcessorFactory procFactory) {

    injectionContext.mapElementType(WiringElementType.SingletonBean, ApplicationScoped.class);
    injectionContext.mapElementType(WiringElementType.DependentBean, Dependent.class);
    injectionContext.mapElementType(WiringElementType.ProducerElement, Produces.class);

    final Set<MetaClass> knownObserverTypes = new HashSet<MetaClass>();

    if (context.getGeneratorContext() != null && context.getGeneratorContext().getTypeOracle() != null) {
      for (JPackage pkg : context.getGeneratorContext().getTypeOracle().getPackages()) {
        TypeScan:
        for (JClassType type : pkg.getTypes()) {
          if (type.isAbstract() || type.isInterface() != null
                  || type.getQualifiedSourceName().startsWith("java.")) continue;

          if (!type.isDefaultInstantiable()) {
            boolean hasInjectableConstructor = false;
            for (JConstructor c : type.getConstructors()) {
              if (injectionContext.isElementType(WiringElementType.InjectionPoint, c)) {
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

          MetaClass clazz = GWTClass.newInstance(type.getOracle(), type);

          injectionContext.addPsuedoScopeForType(clazz);

          for (MetaMethod method : clazz.getMethods()) {
            for (MetaParameter parameter : method.getParameters()) {
              if (parameter.isAnnotationPresent(Observes.class)) {
                knownObserverTypes.add(parameter.getType());
              }
            }
          }
        }
      }
    }

    final MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    final Set<MetaClass> knownTypesWithSuperTypes = new HashSet<MetaClass>(knownObserverTypes);
    for (MetaClass cls : knownObserverTypes) {
      for (Class subClass : scanner.getSubTypesOf(cls.asClass())) {
        knownTypesWithSuperTypes.add(MetaClassFactory.get(subClass));
      }
    }

    addTypeHeirarchyFor(context, knownTypesWithSuperTypes);

    context.append(Stmt.nestedCall(Stmt.newObject(CDI.class))
                .invoke("__resetSubsystem"));

    context.append(Stmt.nestedCall(Stmt.newObject(CDI.class))
            .invoke("initLookupTable", Stmt.invokeStatic(CDIEventTypeLookup.class, "get")));
  }

  public static void addTypeHeirarchyFor(IOCProcessingContext context, final Set<MetaClass> classes) {
    for (final MetaClass subClass : classes) {
      MetaClass cls = subClass;
      do {
        if (cls != subClass) {
          context.append(Stmt.invokeStatic(CDIEventTypeLookup.class, "get")
                  .invoke("addLookup", subClass.getFullyQualifiedName(), cls.getFullyQualifiedName()));
        }

        for (MetaClass interfaceClass : cls.getInterfaces()) {
          context.append(Stmt.invokeStatic(CDIEventTypeLookup.class, "get")
                  .invoke("addLookup", subClass.getFullyQualifiedName(), interfaceClass.getFullyQualifiedName()));

        }
      }
      while ((cls = cls.getSuperClass()) != null);
    }

  }

  public void afterInitialization(IOCProcessingContext context, InjectionContext injectorFactory,
                                  IOCProcessorFactory procFactory) {
  }
}
