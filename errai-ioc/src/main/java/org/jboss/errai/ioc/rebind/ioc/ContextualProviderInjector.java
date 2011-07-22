/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

import java.lang.annotation.Annotation;

import org.jboss.errai.ioc.client.ContextualProviderContext;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Refs;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;

import javax.inject.Provider;

public class ContextualProviderInjector extends TypeInjector {
  private final Injector providerInjector;

  public ContextualProviderInjector(MetaClass type, MetaClass providerType) {
    super(type);
    this.providerInjector = new TypeInjector(providerType);
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
    Injector contextInjector = null;

    if (pType == null) {
      if (providerInjector.getInjectedType().isAssignableTo(Provider.class)) {
        contextInjector = new ContextualProviderContextInjector();
        injectContext.registerInjector(contextInjector);

        statement = Stmt.nestedCall(providerInjector.getType(injectContext, injectableInstance))
                .invoke("get");

        injectContext.deregisterInjector(contextInjector);
      }
      else {

        statement = Stmt.nestedCall(providerInjector.getType(injectContext, injectableInstance))
                .invoke("provide", new Class[0]);
      }

    }
    else {
      MetaType[] typeArgs = pType.getTypeParameters();
      Annotation[] qualifiers = injectableInstance.getQualifiers();

      if (providerInjector.getInjectedType().isAssignableTo(Provider.class)) {
        contextInjector = new ContextualProviderContextInjector(qualifiers, typeArgs);
        injectContext.registerInjector(contextInjector);

        statement = Stmt.nestedCall(providerInjector.getType(injectContext, injectableInstance))
                .invoke("get");

        injectContext.deregisterInjector(contextInjector);
      }
      else {

        statement = Stmt.nestedCall(providerInjector.getType(injectContext, injectableInstance))
                .invoke("provide", typeArgs, qualifiers.length != 0 ? qualifiers : null);

      }
    }

    if (singleton) {
      if (!injected) {
        injectContext.getProcessingContext().append(Stmt.declareVariable(type).named(varName)
                .initializeWith(statement));
      }
      statement = Refs.get(varName);
    }

    injected = true;

    return statement;
  }

  @Override
  public Statement instantiateOnly(InjectionContext injectContext, InjectableInstance injectableInstance) {
    injected = true;
    return providerInjector.getType(injectContext, injectableInstance);
  }

  private static class ContextualProviderContextInjector extends Injector {
    private Annotation[] annotations = new Annotation[0];
    private MetaType[] typeArguments = new MetaType[0];

    private ContextualProviderContextInjector() {
    }

    private ContextualProviderContextInjector(Annotation[] annotations, MetaType[] typeArguments) {
      this.annotations = annotations;
      this.typeArguments = typeArguments;
    }

    @Override
    public Statement instantiateOnly(InjectionContext injectContext, InjectableInstance injectableInstance) {
      return ObjectBuilder.newInstanceOf(ContextualProviderContext.class)
              .extend()
              .publicOverridesMethod("getQualifiers")
              .append(Stmt.load(annotations).returnValue())
              .finish()
              .publicOverridesMethod("getTypeArguments")
              .append(Stmt.load(MetaClassFactory.asClassArray(typeArguments)).returnValue())
              .finish()
              .finish();
    }

    @Override
    public Statement getType(InjectionContext injectContext, InjectableInstance injectableInstance) {
      return instantiateOnly(injectContext, injectableInstance);
    }

    @Override
    public boolean isInjected() {
      return false;
    }

    @Override
    public boolean isSingleton() {
      return false;
    }

    @Override
    public String getVarName() {
      return null;
    }

    @Override
    public MetaClass getInjectedType() {
      return MetaClassFactory.get(ContextualProviderContext.class);
    }
  }
}