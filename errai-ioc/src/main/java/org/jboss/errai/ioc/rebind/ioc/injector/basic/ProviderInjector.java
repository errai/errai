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

package org.jboss.errai.ioc.rebind.ioc.injector.basic;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectorFactory;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;


public class ProviderInjector extends TypeInjector {
  private final AbstractInjector providerInjector;
  private boolean provided = false;

  public ProviderInjector(final MetaClass type,
                          final MetaClass providerType,
                          final InjectionContext context) {
    super(type, context);

    if (EnvUtil.isProdMode()) {
      setEnabled(context.isReachable(type) || context.isReachable(providerType));
    }

    this.providerInjector = (AbstractInjector)
        context.getInjectorFactory().getTypeInjector(providerType, context);

    context.registerInjector(providerInjector);
    providerInjector.setEnabled(isEnabled());

    this.testMock = context.isElementType(WiringElementType.TestMockBean, providerType);
    this.singleton = context.isElementType(WiringElementType.SingletonBean, providerType);
    this.alternative = context.isElementType(WiringElementType.AlternativeBean, type);
    setRendered(true);
  }

  @Override
  public void renderProvider(InjectableInstance injectableInstance) {
    super.renderProvider(injectableInstance);
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {


    if (isSingleton() && provided) {
      return Stmt.loadVariable(providerInjector.getInstanceVarName()).invoke("get");
    }

    provided = true;

    return Stmt.nestedCall(providerInjector.getBeanInstance(injectableInstance)).invoke("get");
  }
}
