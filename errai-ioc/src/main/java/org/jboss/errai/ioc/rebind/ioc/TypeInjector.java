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
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.qualifiers.Any;
import org.jboss.errai.ioc.client.container.CreationalCallback;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class TypeInjector extends Injector {
  protected final MetaClass type;
  protected boolean injected;
  protected boolean singleton;
  protected boolean psuedo;
  protected String varName;

  public TypeInjector(MetaClass type, IOCProcessingContext context) {
    this(type, context, new Annotation[0]);
  }

  public TypeInjector(MetaClass type, IOCProcessingContext context, Annotation[] additionalQualifiers) {
    this.type = type;
    this.singleton = type.isAnnotationPresent(Singleton.class)
            || type.isAnnotationPresent(com.google.inject.Singleton.class)
            || type.isAnnotationPresent(EntryPoint.class);

    this.varName = InjectUtil.getNewVarName();

    try {
      Set<Annotation> qualifiers = new HashSet<Annotation>();
      qualifiers.addAll(InjectUtil.extractQualifiersFromType(type));
      qualifiers.addAll(Arrays.asList(additionalQualifiers));

      if (!qualifiers.isEmpty()) {

        qualifyingMetadata = context.getQualifyingMetadataFactory().createFrom(qualifiers.toArray(new
                Annotation[qualifiers.size()]));

      }
      else {
        qualifyingMetadata = context.getQualifyingMetadataFactory().createDefaultMetadata();
      }
    }
    catch (Throwable e) {
      // ignore
    }
  }

  @Override
  public Statement getType(InjectionContext injectContext, InjectableInstance injectableInstance) {
    Statement val = _getType(injectContext, injectableInstance);
    registerWithBeanManager(injectContext, val);
    return val;
  }

  private Statement _getType(InjectionContext injectContext, InjectableInstance injectableInstance) {
    if (isInjected()) {
      if (isSingleton()) {
        return Refs.get(varName);
      }
      else {
        /**
         * Ensure each permutation of qualifier meta data results in a unique wiring scenario
         */
        final Set<Annotation> fromCompare = new HashSet<Annotation>(Arrays.asList(qualifyingMetadata.getQualifiers()));
        final Set<Annotation> toCompare;
        if (injectableInstance == null
                || injectableInstance.getQualifiers() == null
                || injectableInstance.getQualifiers().length == 0) {
          toCompare = new HashSet<Annotation>(Arrays.asList(injectContext.getProcessingContext()
                  .getQualifyingMetadataFactory().createDefaultMetadata().getQualifiers()));
        }
        else {
          toCompare = new HashSet<Annotation>(Arrays.asList(injectableInstance.getQualifiers()));
        }

        if (fromCompare.equals(toCompare)) {
          return Stmt.loadVariable(varName).invoke("getInstance");
        }
      }
    }

    IOCProcessingContext ctx = injectContext.getProcessingContext();

    MetaClass creationCallbackRef
            = MetaClassFactory.parameterizedAs(CreationalCallback.class, MetaClassFactory.typeParametersOf(type));

    BlockBuilder<AnonymousClassStructureBuilder> callbackBuilder = ObjectBuilder.newInstanceOf(creationCallbackRef).extend()
            .publicOverridesMethod("getInstance");

    ctx.pushBlockBuilder(callbackBuilder);

    InjectUtil.getConstructionStrategy(this, injectContext).generateConstructor(new ConstructionStatusCallback() {
      @Override
      public void callback(boolean constructed) {
        injected = true;
      }
    });

    ctx.append(Stmt.loadVariable(varName).returnValue());

    ctx.popBlockBuilder();

    String creationalCallbackVar = InjectUtil.getNewVarName();

    ctx.globalAppend(Stmt.declareVariable(creationCallbackRef).asFinal().named(creationalCallbackVar)
            .initializeWith(callbackBuilder.finish().finish()));

    if (isSingleton()) {

      ctx.globalAppend(Stmt.declareVariable(type).asFinal().named(varName)
              .initializeWith(Stmt.loadVariable(creationalCallbackVar).invoke("getInstance")));

      return Refs.get(varName);
    }
    else {
      varName = creationalCallbackVar;
      return Stmt.loadVariable(varName).invoke("getInstance");
    }
  }

  @Override
  public Statement instantiateOnly(InjectionContext injectContext, InjectableInstance injectableInstance) {
    return getType(injectContext, injectableInstance);
  }

  @Override
  public boolean isInjected() {
    return injected;
  }

  @Override
  public boolean isSingleton() {
    return singleton;
  }

  public void setSingleton(boolean singleton) {
    this.singleton = singleton;
  }


  public boolean isPseudo() {
    return psuedo;
  }

  public void setPsuedo(boolean psuedo) {
    this.psuedo = psuedo;
  }

  @Override
  public String getVarName() {
    return varName;
  }

  @Override
  public MetaClass getInjectedType() {
    return type;
  }

  private void registerWithBeanManager(InjectionContext context, Statement valueRef) {
    if (useBeanManager) {
      if (InjectUtil.checkIfTypeNeedsAddingToBeanStore(context, this)) {
        if (isSingleton()) {
          context.getProcessingContext().appendToEnd(
                  Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
                          .invoke("addSingletonBean", type, valueRef, qualifyingMetadata.render())
          );
        }
        else {
          context.getProcessingContext().appendToEnd(
                  Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
                          .invoke("addDependentBean", type, Refs.get(varName), qualifyingMetadata.render()));
        }
      }
    }
  }
}
