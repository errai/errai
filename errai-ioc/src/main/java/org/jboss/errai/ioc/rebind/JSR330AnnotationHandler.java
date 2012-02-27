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

package org.jboss.errai.ioc.rebind;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.InjectableInstance;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public abstract class JSR330AnnotationHandler<T extends Annotation> implements AnnotationHandler<T> {
  @Override
  public Set<RequiredDependency> checkDependencies(InjectableInstance instance, T annotation,
                                                   IOCProcessingContext context) {

    Set<RequiredDependency> dependencies = new HashSet<RequiredDependency>();

    MetaClass mc = instance.getType();

    do {
      for (MetaField field : mc.getDeclaredFields()) {
        if (field.isAnnotationPresent(Inject.class)) {
          dependencies.add(new RequiredDependency(field.getType(), InjectUtil.extractQualifiersFromField(field)));
        }
      }
      
      for (MetaMethod method : mc.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Inject.class)) {
          for (MetaParameter parm : method.getParameters()) {
            dependencies.add(new RequiredDependency(parm.getType(), InjectUtil.extractQualifiersFromParameter(parm)));
          }
        }
      }

      for (MetaConstructor constructor : mc.getConstructors()) {
        if (constructor.isAnnotationPresent(Inject.class)) {
          for (MetaParameter parm : constructor.getParameters()) {
            dependencies.add(new RequiredDependency(parm.getType(), InjectUtil.extractQualifiersFromParameter(parm),
                    DependencyPolicy.BeforeMandatory));
          }
        }
      }

    }
    while ((mc = mc.getSuperClass()) != null);

    return dependencies;
  }
}
