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


import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.ioc.client.container.BeanRef;
import org.jboss.errai.ioc.client.container.CreationalCallback;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;

import javax.enterprise.inject.New;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder.newInstanceOf;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.framework.util.Stmt.declareVariable;
import static org.jboss.errai.codegen.framework.util.Stmt.load;
import static org.jboss.errai.codegen.framework.util.Stmt.loadVariable;

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
          return loadVariable(creationalCallbackVarName).invoke("getInstance", Refs.get("context"));
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
          return loadVariable(creationalCallbackVarName).invoke("getInstance", Refs.get("context"));
        }
      }
    }

    IOCProcessingContext ctx = injectContext.getProcessingContext();

    MetaClass creationCallbackRef = parameterizedAs(CreationalCallback.class, typeParametersOf(type));

    final BlockBuilder<AnonymousClassStructureBuilder> callbackBuilder = newInstanceOf(creationCallbackRef).extend()
            .publicOverridesMethod("getInstance", Parameter.of(CreationalContext.class, "context", true));

    callbackBuilder.append(declareVariable(Class.class).named("beanType").initializeWith(load(type)));
    callbackBuilder.append(declareVariable(Annotation[].class).named("qualifiers")
            .initializeWith(load(qualifyingMetadata.getQualifiers())));

    ctx.pushBlockBuilder(callbackBuilder);

    InjectUtil.getConstructionStrategy(this, injectContext).generateConstructor(new ConstructionStatusCallback() {
      @Override
      public void callback(boolean constructed) {
        callbackBuilder.append(declareVariable(BeanRef.class).named("beanRef")
                .initializeWith(loadVariable("context").invoke("getBeanReference", Refs.get("beanType"),
                        Refs.get("qualifiers"))));

        callbackBuilder.append(loadVariable("context").invoke("addBean", Refs.get("beanRef"), Refs.get(varName)));
        injected = true;
      }
    });

    ctx.popBlockBuilder();

    creationalCallbackVarName = InjectUtil.getNewVarName();

    ctx.globalAppend(declareVariable(creationCallbackRef).asFinal().named(creationalCallbackVarName)
            .initializeWith(callbackBuilder.finish().finish()));

    Statement retVal;

    if (isSingleton()) {
      ctx.globalAppend(declareVariable(type).asFinal().named(varName)
              .initializeWith(loadVariable(creationalCallbackVarName).invoke("getInstance",
                      Refs.get("context"))));

      retVal = Refs.get(varName);
    }
    else {
      retVal = loadVariable(creationalCallbackVarName).invoke("getInstance", Refs.get("context"));
    }

    if (injectContext.isProxiedInjectorAvailable(type, qualifyingMetadata)) {
      ProxyInjector proxyInjector = (ProxyInjector) injectContext.getProxiedInjector(type, qualifyingMetadata);
      if (!proxyInjector.isProxied()) {
        proxyInjector.setProxied(true);
        proxyInjector.setProxyStatement(retVal);
      }
    }

    callbackBuilder.append(loadVariable(varName).returnValue());

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

  public String getCreationalCallbackVarName() {
    return creationalCallbackVarName;
  }

  private void registerWithBeanManager(InjectionContext context, Statement valueRef) {
    if (useBeanManager) {
      if (InjectUtil.checkIfTypeNeedsAddingToBeanStore(context, this)) {
        Statement initCallbackRef;
        if (getPostInitCallbackVar() == null) {
          initCallbackRef = load(null);
        }
        else {
          initCallbackRef = loadVariable(getPostInitCallbackVar());
        }

        if (isSingleton()) {
          context.getProcessingContext().appendToEnd(
                  loadVariable(context.getProcessingContext().getContextVariableReference())
                          .invoke("addSingletonBean", type, valueRef,
                                  qualifyingMetadata.render(), initCallbackRef)
          );
        }
        else {
          context.getProcessingContext().appendToEnd(
                  loadVariable(context.getProcessingContext().getContextVariableReference())
                          .invoke("addDependentBean", type, Refs.get(creationalCallbackVarName),
                                  qualifyingMetadata.render(), initCallbackRef));
        }
      }
    }
  }
}
