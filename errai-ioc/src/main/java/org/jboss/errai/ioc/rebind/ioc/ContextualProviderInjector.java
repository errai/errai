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

package org.jboss.errai.ioc.rebind.ioc;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.impl.java.JavaReflectionField;
import org.jboss.errai.codegen.framework.meta.impl.java.JavaReflectionParameterizedType;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;

import javax.enterprise.inject.Alternative;
import java.lang.annotation.Annotation;

public class ContextualProviderInjector extends TypeInjector {
  private final Injector providerInjector;

  public ContextualProviderInjector(MetaClass type, MetaClass providerType, InjectorFactory factory) {
    super(type, factory.getInjectionContext().getProcessingContext());
    this.providerInjector = new TypeInjector(providerType, factory.getInjectionContext().getProcessingContext());
    factory.addInjector(providerInjector);

    this.singleton = factory.getInjectionContext().getProcessingContext()
            .isSingletonScope(providerType.getAnnotations());
    this.alternative = providerType.isAnnotationPresent(Alternative.class);
    injected = true;
  }

  @Override
  public Statement getType(InjectionContext injectContext, InjectableInstance injectableInstance) {
    MetaClass type = null;
    MetaParameterizedType pType = null;

    switch (injectableInstance.getTaskType()) {
      case PrivateField:
      case Field:
        MetaField field = injectableInstance.getField();
        type = field.getType();

        pType = type.getParameterizedType();
        // TODO refactor!
        if (pType == null && field instanceof JavaReflectionField) {
          pType = (JavaReflectionParameterizedType) field.getGenericType();
        }
        break;

      case Parameter:
        MetaParameter parm = injectableInstance.getParm();
        type = parm.getType();

        pType = type.getParameterizedType();
        break;
    }

    Statement statement;

    MetaType[] typeArgs = pType.getTypeParameters();
    MetaClass[] typeArgsClasses = new MetaClass[typeArgs.length];

    for (int i = 0; i < typeArgs.length; i++) {
      MetaType argType = typeArgs[i];

      if (argType instanceof MetaClass) {
        typeArgsClasses[i] = (MetaClass) argType;
      }
      else if (argType instanceof MetaParameterizedType) {
        typeArgsClasses[i] = (MetaClass) ((MetaParameterizedType) argType).getRawType();
      }
    }

    Annotation[] qualifiers = injectableInstance.getQualifiers();


    if (providerInjector.isSingleton()) {
      if (!providerInjector.isInjected()) {


        providerInjector.getType(injectContext, injectableInstance);
//        injectContext.getProcessingContext().globalAppend(Stmt.declareVariable(type).asFinal().named(getVarName())
//                .initializeWith(providerInjector.getType(injectContext, injectableInstance)));

      }
      return Stmt.loadVariable(providerInjector.getVarName()).invoke("provide", typeArgsClasses, qualifiers.length != 0 ? qualifiers : null);
    }
    else {
//      provided = true;
      return Stmt.nestedCall(providerInjector.getType(injectContext, injectableInstance))
              .invoke("provide", typeArgsClasses, qualifiers.length != 0 ? qualifiers : null);
    }


  }


  @Override
  public Statement instantiateOnly(InjectionContext injectContext, InjectableInstance injectableInstance) {
    return providerInjector.getType(injectContext, injectableInstance);
  }

}