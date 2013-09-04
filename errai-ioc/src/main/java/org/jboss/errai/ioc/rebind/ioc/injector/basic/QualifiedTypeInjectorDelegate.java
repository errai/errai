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
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

/**
 * This injector wraps another injector to create qualifying references based on type parameters and qualifiers
 * to the underlying bean. For instance if two beans implement a common interface, with two different type
 * parameters, each bean will be wrapped in this injector and added as common injectors to the interface.
 *
 * @author Mike Brock
 */
public class QualifiedTypeInjectorDelegate extends AbstractInjector {
  protected final MetaClass type;
  protected final Injector delegate;

  public QualifiedTypeInjectorDelegate(final MetaClass type,
                                       final Injector delegate,
                                       final MetaParameterizedType parameterizedType) {
    this.type = type;
    this.delegate = delegate;

    this.qualifyingTypeInformation = parameterizedType;
    this.qualifyingMetadata = getMetadataWithAny(delegate.getQualifyingMetadata());

    delegate.addRegistrationHook(
        new RegistrationHook() {
          @Override
          public void onRegister(final InjectionContext context, final Statement beanValue) {
            registerWithBeanManager(context, beanValue);
          }
        }
    );
  }

  @Override
  public void renderProvider(InjectableInstance injectableInstance) {
  }

  @Override
  public boolean isRendered() {
    return delegate.isRendered();
  }

  @Override
  public boolean isTestMock() {
    return delegate.isTestMock();
  }

  @Override
  public boolean isSingleton() {
    return delegate.isSingleton();
  }

  @Override
  public boolean isPseudo() {
    return delegate.isPseudo();
  }

  @Override
  public String getInstanceVarName() {
    return delegate.getInstanceVarName();
  }

  @Override
  public MetaClass getInjectedType() {
    return delegate.getInjectedType();
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {
    return delegate.getBeanInstance(injectableInstance);
  }

  @Override
  public boolean isDependent() {
    return delegate.isDependent();
  }

  @Override
  public boolean isProvider() {
    return delegate.isProvider();
  }

  @Override
  public MetaClass getEnclosingType() {
    return delegate.getEnclosingType();
  }

  @Override
  public String getPostInitCallbackVar() {
    return delegate.getPostInitCallbackVar();
  }

  @Override
  public String getPreDestroyCallbackVar() {
    return delegate.getPreDestroyCallbackVar();
  }

  @Override
  public void setPostInitCallbackVar(final String var) {
    delegate.setPostInitCallbackVar(var);
  }

  @Override
  public void setPreDestroyCallbackVar(final String preDestroyCallbackVar) {
    delegate.setPreDestroyCallbackVar(preDestroyCallbackVar);
  }

  @Override
  public String getCreationalCallbackVarName() {
    return delegate.getCreationalCallbackVarName();
  }

  @Override
  public void registerWithBeanManager(final InjectionContext context,
                                      final Statement valueRef) {

    if (InjectUtil.checkIfTypeNeedsAddingToBeanStore(context, this)) {
      final QualifyingMetadata md = delegate.getQualifyingMetadata();
      context.getProcessingContext().appendToEnd(
          Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
              .invoke("addBean", type, delegate.getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                  isSingleton() ? valueRef : null, md.render(), delegate.getBeanName(), false));

      for (final RegistrationHook hook : getRegistrationHooks()) {
        hook.onRegister(context, valueRef);
      }
    }
  }

  @Override
  public MetaClass getConcreteInjectedType() {
    Injector inj = delegate;
    while (inj instanceof QualifiedTypeInjectorDelegate) {
      inj = ((QualifiedTypeInjectorDelegate) inj).delegate;
    }
    return inj.getInjectedType();
  }

  @Override
  public boolean isAlternative() {
    return delegate.isAlternative();
  }

  @Override
  public boolean isSoftDisabled() {
    return super.isSoftDisabled();
  }

  @Override
  public void setSoftDisabled(final boolean softDisabled) {
    super.setSoftDisabled(softDisabled);
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && delegate.isEnabled();
  }

  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  public Injector getDelegate() {
    return delegate;
  }
}
