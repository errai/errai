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


import org.jboss.errai.codegen.framework.Cast;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.ProxyMaker;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.client.container.CreationalCallback;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;

import javax.enterprise.inject.New;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TypeInjector extends Injector {
  protected final MetaClass type;
  protected boolean injected;
  protected boolean singleton;
  protected boolean psuedo;
  protected String varName;
  protected String creationalCallbackVarName;

  public TypeInjector(MetaClass type, IOCProcessingContext context) {
    this(type, context, new Annotation[0]);
  }

  public TypeInjector(MetaClass type, IOCProcessingContext context, Annotation[] additionalQualifiers) {
    this.type = type;
    this.singleton = context.isSingletonScope(type.getAnnotations());
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
        if (!hasNewQualifier(injectableInstance)) {
          return Refs.get(varName);
        }
        else if (creationalCallbackVarName != null) {
          return Stmt.loadVariable(creationalCallbackVarName).invoke("getInstance", Refs.get("context"));
        }
      }
      else if (creationalCallbackVarName != null) {
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
          return Stmt.loadVariable(creationalCallbackVarName).invoke("getInstance", Refs.get("context"));
        }
      }
    }

    IOCProcessingContext ctx = injectContext.getProcessingContext();

    MetaClass creationCallbackRef
            = MetaClassFactory.parameterizedAs(CreationalCallback.class, MetaClassFactory.typeParametersOf(type));

    BlockBuilder<AnonymousClassStructureBuilder> callbackBuilder = ObjectBuilder.newInstanceOf(creationCallbackRef).extend()
            .publicOverridesMethod("getInstance", Parameter.of(CreationalContext.class, "context"));
    
    callbackBuilder.append(Stmt.declareVariable(Class.class).named("beanType").initializeWith(Stmt.load(type)));
    callbackBuilder.append(Stmt.declareVariable(Annotation[].class).named("qualifiers")
            .initializeWith(Stmt.load(qualifyingMetadata.getQualifiers())));

    ctx.pushBlockBuilder(callbackBuilder);

    InjectUtil.getConstructionStrategy(this, injectContext).generateConstructor(new ConstructionStatusCallback() {
      @Override
      public void callback(boolean constructed) {
        injected = true;
      }
    });


    ctx.popBlockBuilder();

    creationalCallbackVarName = InjectUtil.getNewVarName();

    ctx.globalAppend(Stmt.declareVariable(creationCallbackRef).asFinal().named(creationalCallbackVarName)
            .initializeWith(callbackBuilder.finish().finish()));

    Statement retVal;

    if (isSingleton()) {
      ctx.globalAppend(Stmt.declareVariable(type).asFinal().named(varName)
              .initializeWith(Stmt.loadVariable(creationalCallbackVarName).invoke("getInstance",
                      Refs.get("context"))));

      retVal = Refs.get(varName);
    }
    else {
      retVal = Stmt.loadVariable(creationalCallbackVarName).invoke("getInstance", Refs.get("context"));
    }

    if (injectContext.isProxiedInjectorAvailable(type, qualifyingMetadata)) {
      ProxyInjector proxyInjector = (ProxyInjector) injectContext.getProxiedInjector(type, qualifyingMetadata);
      if (!proxyInjector.isProxied()) {
//        callbackBuilder.append(
//                Stmt.nestedCall(Cast.to(proxyInjector.getProxyClass(), Stmt.loadVariable("context").invoke("getUnresolvedProxy",
//                        type, qualifyingMetadata.getQualifiers())))
//                        .invoke(ProxyMaker.PROXY_BIND_METHOD, Refs.get(varName)));
        proxyInjector.setProxied(true);
        proxyInjector.setProxyStatement(retVal);

      }
    }

    callbackBuilder.append(Stmt.loadVariable("context").invoke("addBean", Refs.get(varName), Refs.get("beanType"),
            Refs.get("qualifiers")));

    callbackBuilder.append(Stmt.loadVariable(varName).returnValue());

    return retVal;
  }

  private static boolean hasNewQualifier(InjectableInstance instance) {
    if (instance != null) {
      for (Annotation annotation : instance.getQualifiers()) {
        if (annotation.annotationType().equals(New.class)) return true;
      }
    }
    return false;
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
        Statement initCallbackRef;
        if (getPostInitCallbackVar() == null) {
          initCallbackRef = Stmt.load(null);
        }
        else {
          initCallbackRef = Stmt.loadVariable(getPostInitCallbackVar());
        }

        if (isSingleton()) {
          context.getProcessingContext().appendToEnd(
                  Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
                          .invoke("addSingletonBean", type, valueRef,
                                  qualifyingMetadata.render(), initCallbackRef)
          );
        }
        else {
          context.getProcessingContext().appendToEnd(
                  Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
                          .invoke("addDependentBean", type, Refs.get(creationalCallbackVarName),
                                  qualifyingMetadata.render(), initCallbackRef));
        }
      }
    }
  }
}
