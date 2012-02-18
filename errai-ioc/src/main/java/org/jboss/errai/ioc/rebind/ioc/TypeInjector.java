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
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;

import javax.inject.Singleton;
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
    Statement val = _getType(injectContext);
    registerWithBeanManager(injectContext, val);
    return val;
  }

  private Statement _getType(InjectionContext injectContext) {
    if (isInjected()) {
      if (isSingleton()) {
        return Refs.get(varName);
      }
      else {
        varName = InjectUtil.getNewVarName();
      }
    }

    InjectUtil.getConstructionStrategy(this, injectContext).generateConstructor(new ConstructionStatusCallback() {
      @Override
      public void callback(boolean constructed) {
        injected = true;
      }
    });


    return Refs.get(varName);
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
      BlockBuilder<?> b = context.getProcessingContext().getBlockBuilder();

      b.append(Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
              .invoke("addBean", type, valueRef, qualifyingMetadata.getQualifiers()));
    }
  }
}
