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

package org.jboss.errai.ioc.rebind.ioc.extension;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.graph.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.GraphBuilder;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.mvel2.util.NullType;

import com.google.gwt.core.ext.GeneratorContext;

/**
 * @author Mike Brock
 */
public abstract class JSR330AnnotationHandler<T extends Annotation> implements AnnotationHandler<T> {

  @Override
  public void registerMetadata(InjectableInstance instance, T annotation, IOCProcessingContext procCtx) {
  }

  @Override
  public void getDependencies(DependencyControl control, InjectableInstance instance,
                              T annotation,
                              IOCProcessingContext context) {

    MetaClass mc = instance.getEnclosingType();
    processDependencies(control, mc, instance.getInjectionContext());
  }

  public static void processDependencies(final DependencyControl control,
                                         final MetaClass metaClass,
                                         final InjectionContext context) {

    final GraphBuilder graphBuilder = context.getGraphBuilder();
    final GeneratorContext genCtx = context.getProcessingContext().getGeneratorContext();
    MetaClass mc = metaClass;
    do {
      for (MetaField field : mc.getDeclaredFields()) {

        if (context.isElementType(WiringElementType.InjectionPoint, field)) {
          control.notifyDependency(field.getType());

          for (MetaClass cls : fillInInterface(field.getType(), genCtx)) {
            graphBuilder.addDependency(field.getType(), Dependency.on(cls));
          }
        }
      }

      for (MetaMethod method : mc.getDeclaredMethods()) {
        if (context.isElementType(WiringElementType.InjectionPoint, method)) {
          for (MetaParameter parm : method.getParameters()) {
            control.notifyDependency(parm.getType());

            for (MetaClass cls : fillInInterface(parm.getType(), genCtx)) {
              graphBuilder.addDependency(parm.getType(), Dependency.on(cls));
            }
          }
        }
      }

      for (MetaConstructor constructor : mc.getConstructors()) {
        if (context.isElementType(WiringElementType.InjectionPoint, constructor)) {
          for (MetaParameter parm : constructor.getParameters()) {
            control.notifyDependency(parm.getType());

            for (MetaClass cls : fillInInterface(parm.getType(), genCtx)) {
              graphBuilder.addDependency(parm.getType(), Dependency.on(cls));
            }
          }
        }
      }
    }
    while ((mc = mc.getSuperClass()) != null);
  }

  public static <T> Set<MetaClass> fillInInterface(final MetaClass cls, final GeneratorContext genCtx) {
    if (NullType.class.getName().equals(cls.getFullyQualifiedName())) {
      return Collections.emptySet();
    }

    if (cls.isInterface()) {
      final Collection<MetaClass> subTypes = ClassScanner.getSubTypesOf(cls, genCtx);
      final Set<MetaClass> deps = new HashSet<MetaClass>();
      for (final MetaClass c : subTypes) {
        if (c.isSynthetic() || c.isAnonymousClass()) continue;
        if (c.isPublic()) {
          deps.add(c);
        }
      }

      return deps;
    }
    else {
      return Collections.emptySet();
    }
  }
}
