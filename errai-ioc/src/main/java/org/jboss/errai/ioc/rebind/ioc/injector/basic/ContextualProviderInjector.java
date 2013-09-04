/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.injector.basic;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.lang.annotation.Annotation;

public class ContextualProviderInjector extends TypeInjector {
  private final Injector providerInjector;

  public ContextualProviderInjector(final MetaClass type,
                                    final MetaClass providerType,
                                    final InjectionContext context) {
    super(type, context);
    this.providerInjector = context.getInjectorFactory().getTypeInjector(providerType, context);
    context.registerInjector(providerInjector);

    this.testMock = context.isElementType(WiringElementType.TestMockBean, providerType);
    this.singleton = context.isElementType(WiringElementType.SingletonBean, providerType);
    this.alternative = context.isElementType(WiringElementType.AlternativeBean, providerType);

    setRendered(true);
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {
    final MetaClass type;
    MetaParameterizedType pType;

    switch (injectableInstance.getTaskType()) {
      case Type:
        return null;
      case PrivateField:
      case Field:
        final MetaField field = injectableInstance.getField();
        type = field.getType();

        pType = type.getParameterizedType();
        break;

      case Parameter:
        final MetaParameter parm = injectableInstance.getParm();
        type = parm.getType();

        pType = type.getParameterizedType();
        break;

      default:
        throw new RuntimeException("illegal task type: " + injectableInstance.getEnclosingType());
    }

    final MetaType[] typeArgs = pType.getTypeParameters();
    final MetaClass[] typeArgsClasses = new MetaClass[typeArgs.length];

    for (int i = 0; i < typeArgs.length; i++) {
      final MetaType argType = typeArgs[i];

      if (argType instanceof MetaClass) {
        typeArgsClasses[i] = (MetaClass) argType;
      }
      else if (argType instanceof MetaParameterizedType) {
        typeArgsClasses[i] = (MetaClass) ((MetaParameterizedType) argType).getRawType();
      }
    }

    final Annotation[] qualifiers = injectableInstance.getQualifiers();

    if (providerInjector.isSingleton() && providerInjector.isRendered()) {
      return Stmt.loadVariable(providerInjector.getInstanceVarName()).invoke("provide", typeArgsClasses,
          qualifiers.length != 0 ? qualifiers : null);
    }
    else {
      return Stmt.nestedCall(providerInjector.getBeanInstance(injectableInstance))
              .invoke("provide", typeArgsClasses, qualifiers.length != 0 ? qualifiers : null);
    }
  }

  @Override
  public boolean matches(MetaParameterizedType parameterizedType, QualifyingMetadata qualifyingMetadata) {
    boolean parmTypesSatisfied = true;
    if (parameterizedType != null) {
      parmTypesSatisfied = parameterizedType.isAssignableFrom(getQualifyingTypeInformation());
    }

    return parmTypesSatisfied;
  }
  
  
}