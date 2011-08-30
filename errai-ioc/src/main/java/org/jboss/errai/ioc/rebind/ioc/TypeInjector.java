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
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.util.Refs;

public class TypeInjector extends Injector {
  protected final MetaClass type;
  protected boolean injected;
  protected boolean singleton;
  protected String varName;

  public TypeInjector(MetaClass type, IOCProcessingContext context) {
    this.type = type;
    this.singleton = type.isAnnotationPresent(Singleton.class)
            || type.isAnnotationPresent(com.google.inject.Singleton.class)
            || type.isAnnotationPresent(EntryPoint.class);

    this.varName = InjectUtil.getNewVarName();

    try {
      Set<Annotation> qualifiers = new HashSet<Annotation>();
      qualifiers.addAll(InjectUtil.extractQualifiersFromType(type));

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
    if (isInjected()) {
      if (isSingleton()) {
        return Refs.get(varName);
      }
      else {
        varName = InjectUtil.getNewVarName();
      }
    }

    InjectUtil.getConstructionStrategy(this, injectContext).generateConstructor();

    injected = true;

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

  @Override
  public String getVarName() {
    return varName;
  }

  @Override
  public MetaClass getInjectedType() {
    return type;
  }
}
