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

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.graph.SortUnit;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

import java.lang.annotation.Annotation;
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
  public Set<SortUnit> getDependencies(DependencyControl control, InjectableInstance instance,
                                       T annotation,
                                       IOCProcessingContext context) {

    Set<SortUnit> dependencies = new HashSet<SortUnit>();
    MetaClass mc = instance.getType();

    do {
      for (MetaField field : mc.getDeclaredFields()) {
        if (instance.getInjectionContext().isElementType(WiringElementType.InjectionPoint, field)) {

          dependencies.add(new SortUnit(field.getType(), InjectUtil.getQualifiersFromAnnotations(field.getAnnotations())));
          dependencies.addAll(fillInInterface(field.getType().asClass()));
        }
      }

      for (MetaMethod method : mc.getDeclaredMethods()) {
        if (instance.getInjectionContext().isElementType(WiringElementType.InjectionPoint, method)) {
          for (MetaParameter parm : method.getParameters()) {
            dependencies.add(new SortUnit(parm.getType(), InjectUtil.getQualifiersFromAnnotations(parm.getAnnotations())));
            dependencies.addAll(fillInInterface(parm.getType().asClass()));
          }
        }
      }

      for (MetaConstructor constructor : mc.getConstructors()) {
        if (instance.getInjectionContext().isElementType(WiringElementType.InjectionPoint, constructor)) {
          for (MetaParameter parm : constructor.getParameters()) {
            dependencies.add(new SortUnit(parm.getType(), InjectUtil.getQualifiersFromAnnotations(parm.getAnnotations())));
            dependencies.addAll(fillInInterface(parm.getType().asClass()));
          }
        }
      }

    }
    while ((mc = mc.getSuperClass()) != null);

    return Collections.unmodifiableSet(dependencies);
  }

  private static <T> Set<SortUnit> fillInInterface(Class<T> cls) {
    if (cls.isInterface()) {
      Set<Class<? extends T>> subTypes = ScannerSingleton.getOrCreateInstance().getSubTypesOf(cls);
      Set<SortUnit> sortUnits = new HashSet<SortUnit>();
      for (Class<? extends T> c : subTypes) {
        sortUnits.add(new SortUnit(MetaClassFactory.get(c)));
      }

      return sortUnits;
    }
    else {
      return Collections.emptySet();
    }

  }
}
