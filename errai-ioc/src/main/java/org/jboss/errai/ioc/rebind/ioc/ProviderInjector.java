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

import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;

import javax.enterprise.inject.Alternative;
import javax.inject.Provider;


public class ProviderInjector extends TypeInjector {
  private final Injector providerInjector;
  private boolean provided = false;
  private boolean standardProvider = false;

  public ProviderInjector(MetaClass type, MetaClass providerType, IOCProcessingContext context) {
    super(type, context);
    this.providerInjector = new TypeInjector(providerType, context);
    this.standardProvider = providerInjector.getInjectedType().isAssignableTo(Provider.class);
    this.singleton = context.isSingletonScope(providerType.getAnnotations());
    this.alternative = type.isAnnotationPresent(Alternative.class);
    this.injected = true;
  }

  @Override
  public Statement getType(InjectionContext injectContext, InjectableInstance injectableInstance) {
    if (isSingleton() && provided) {
      if (standardProvider) {
        return Stmt.loadVariable(providerInjector.getVarName()).invoke("get");
      }
      else {
        return Stmt.loadVariable(providerInjector.getVarName()).invoke("provide");
      }
    }

    //provided = true;

    if (standardProvider) {
      return Stmt.nestedCall(providerInjector.getType(injectContext, injectableInstance))
              .invoke("get");
    }
    else {
      return Stmt.nestedCall(providerInjector.getType(injectContext, injectableInstance))
              .invoke("provide");
    }
  }

  @Override
  public Statement instantiateOnly(InjectionContext injectContext, InjectableInstance injectableInstance) {
    return providerInjector.getType(injectContext, injectableInstance);
  }

}
