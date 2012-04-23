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

import javassist.bytecode.stackmap.TypeData;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.graph.Dependency;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.mvel2.util.NullType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public abstract class JSR330AnnotationHandler<T extends Annotation> implements AnnotationHandler<T> {

  @Override
  public void registerMetadata(InjectableInstance instance, T annotation, IOCProcessingContext context) {
  }

  @Override
  public void getDependencies(DependencyControl control, InjectableInstance instance,
                              T annotation,
                              IOCProcessingContext context) {

    MetaClass mc = instance.getType();
    processDependencies(control, mc, instance.getInjectionContext());
  }

  public static void processDependencies(DependencyControl control, MetaClass mc, InjectionContext context) {

    do {
      for (MetaField field : mc.getDeclaredFields()) {

        if (context.isElementType(WiringElementType.InjectionPoint, field)) {
          control.notifyDependency(field.getType());
          control.notifyDependencies(fillInInterface(field.getType().asClass()));
        }
      }

      for (MetaMethod method : mc.getDeclaredMethods()) {
        if (context.isElementType(WiringElementType.InjectionPoint, method)) {
          for (MetaParameter parm : method.getParameters()) {
            control.notifyDependency(parm.getType());
            control.notifyDependencies(fillInInterface(parm.getType().asClass()));
          }
        }
      }

      for (MetaConstructor constructor : mc.getConstructors()) {
        if (context.isElementType(WiringElementType.InjectionPoint, constructor)) {
          for (MetaParameter parm : constructor.getParameters()) {
            control.notifyDependency(parm.getType());
            control.notifyDependencies(fillInInterface(parm.getType().asClass()));
          }
        }
      }

    }
    while ((mc = mc.getSuperClass()) != null);
  }

  public static <T> Set<MetaClass> fillInInterface(Class<T> cls) {
    if (NullType.class.isAssignableFrom(cls)) {
      return Collections.emptySet();
    }

    if (cls.isInterface()) {
      Set<Class<? extends T>> subTypes = ScannerSingleton.getOrCreateInstance().getSubTypesOf(cls);
      Set<MetaClass> deps = new HashSet<MetaClass>();
      for (Class<? extends T> c : subTypes) {
        if (c.isSynthetic() || c.isAnonymousClass()) continue;
        if (Modifier.isPublic(c.getModifiers())) {
          deps.add(MetaClassFactory.get(c));
        }
      }

      return deps;
    }
    else {
      return Collections.emptySet();
    }
  }
}
