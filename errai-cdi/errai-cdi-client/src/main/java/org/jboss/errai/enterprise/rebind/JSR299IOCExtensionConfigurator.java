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

import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.enterprise.client.cdi.CDIEventTypeLookup;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCConfigProcessor;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import java.util.HashSet;
import java.util.Set;

@IOCExtension
public class JSR299IOCExtensionConfigurator implements IOCExtensionConfigurator {
  public void configure(final IOCProcessingContext context, final InjectionContext injectionContext,
                        final IOCConfigProcessor procFactory) {

    injectionContext.mapElementType(WiringElementType.SingletonBean, ApplicationScoped.class);
    injectionContext.mapElementType(WiringElementType.ProducerElement, Produces.class);

  }

  public static void addTypeHeirarchyFor(final IOCProcessingContext context, final Set<MetaClass> classes) {
    final BlockStatement instanceInitializer = context.getBootstrapClass().getInstanceInitializer();

    for (final MetaClass subClass : classes) {
      MetaClass cls = subClass;
      do {
        if (cls != subClass) {
          instanceInitializer.addStatement(Stmt.invokeStatic(CDIEventTypeLookup.class, "get")
                  .invoke("addLookup", subClass.getFullyQualifiedName(), cls.getFullyQualifiedName()));
        }

        for (final MetaClass interfaceClass : cls.getInterfaces()) {
          instanceInitializer.addStatement(Stmt.invokeStatic(CDIEventTypeLookup.class, "get")
                  .invoke("addLookup", subClass.getFullyQualifiedName(), interfaceClass.getFullyQualifiedName()));

        }
      }
      while ((cls = cls.getSuperClass()) != null);
    }
  }

  public void afterInitialization(final IOCProcessingContext context,
                                  final InjectionContext injectionContext,
                                  final IOCConfigProcessor procFactory) {

    final BlockStatement instanceInitializer = context.getBootstrapClass().getInstanceInitializer();

    final Set<MetaClass> knownObserverTypes = new HashSet<MetaClass>();

    for (final MetaParameter parameter : ClassScanner.getParametersAnnotatedWith(Observes.class)) {
      knownObserverTypes.add(parameter.getType());
    }

    final Set<MetaClass> knownTypesWithSuperTypes = new HashSet<MetaClass>(knownObserverTypes);
    for (final MetaClass cls : knownObserverTypes) {
      for (final MetaClass subClass : ClassScanner.getSubTypesOf(cls)) {
        knownTypesWithSuperTypes.add(subClass);
      }
    }

    addTypeHeirarchyFor(context, knownTypesWithSuperTypes);

    instanceInitializer.addStatement(Stmt.nestedCall(Stmt.newObject(CDI.class))
            .invoke("initLookupTable", Stmt.invokeStatic(CDIEventTypeLookup.class, "get")));
  }
}
