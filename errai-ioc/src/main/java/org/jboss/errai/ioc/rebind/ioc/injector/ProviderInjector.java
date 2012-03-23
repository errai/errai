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

package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

import javax.enterprise.inject.Alternative;
import javax.inject.Provider;


public class ProviderInjector extends TypeInjector {
  private final AbstractInjector providerInjector;
  private boolean provided = false;

  public ProviderInjector(MetaClass type, MetaClass providerType, InjectionContext context) {
    super(type, context);
    this.providerInjector = new TypeInjector(providerType, context);
    context.registerInjector(providerInjector);
    this.singleton = context.isElementType(WiringElementType.SingletonBean, providerType);
    this.alternative = context.isElementType(WiringElementType.AlternativeBean, type);
    this.injected = true;
  }

  @Override
  public Statement getBeanInstance(InjectionContext injectContext, InjectableInstance injectableInstance) {
    if (isSingleton() && provided) {
      return Stmt.loadVariable(providerInjector.getVarName()).invoke("get");
    }

    provided = true;

    return Stmt.nestedCall(providerInjector.getBeanInstance(injectContext, injectableInstance)).invoke("get");
  }
}
