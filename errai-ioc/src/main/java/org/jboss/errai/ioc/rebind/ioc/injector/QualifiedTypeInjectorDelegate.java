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
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class QualifiedTypeInjectorDelegate implements Injector {
  private MetaClass type;
  private Injector delegate;

  public QualifiedTypeInjectorDelegate(MetaClass type, Injector delegate, MetaParameterizedType parameterizedType) {
    this.type = type;
    this.delegate = delegate;
    delegate.setQualifyingTypeInformation(parameterizedType);

    delegate.addRegistrationHook(
           new RegistrationHook() {
             @Override
             public void onRegister(InjectionContext context, Statement beanValue) {
               registerWithBeanManager(context, beanValue);
             }
           }
    );

  }

  @Override
  public Statement getBeanInstance(InjectionContext injectContext, InjectableInstance injectableInstance) {
    Statement val = _getType(injectContext, injectableInstance);
    return val;
  }

  public Statement _getType(InjectionContext injectContext, InjectableInstance injectableInstance) {
    return delegate.getBeanInstance(injectContext, injectableInstance);
  }

  @Override
  public boolean isInjected() {
    return delegate.isInjected();
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
  public String getVarName() {
    return delegate.getVarName();
  }

  @Override
  public MetaClass getInjectedType() {
    return delegate.getInjectedType();
  }

  @Override
  public boolean metadataMatches(Injector injector) {
    return delegate.metadataMatches(injector);
  }

  @Override
  public QualifyingMetadata getQualifyingMetadata() {
    return delegate.getQualifyingMetadata();
  }

  @Override
  public Statement getBeanInstance(InjectableInstance injectableInstance) {
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
  public boolean matches(MetaParameterizedType parameterizedType, QualifyingMetadata qualifyingMetadata) {
    return delegate.matches(parameterizedType, qualifyingMetadata);
  }

  @Override
  public MetaParameterizedType getQualifyingTypeInformation() {
    return delegate.getQualifyingTypeInformation();
  }

  @Override
  public void setQualifyingTypeInformation(MetaParameterizedType qualifyingTypeInformation) {
    delegate.setQualifyingTypeInformation(qualifyingTypeInformation);
  }

  @Override
  public void setPostInitCallbackVar(String var) {
    delegate.setPostInitCallbackVar(var);
  }

  @Override
  public void setPreDestroyCallbackVar(String preDestroyCallbackVar) {
    delegate.setPreDestroyCallbackVar(preDestroyCallbackVar);
  }

  @Override
  public String getCreationalCallbackVarName() {
    return delegate.getCreationalCallbackVarName();
  }

  @Override
  public void setCreationalCallbackVarName(String creationalCallbackVarName) {
    delegate.setCreationalCallbackVarName(creationalCallbackVarName);
  }


  @Override
  public void addRegistrationHook(RegistrationHook registrationHook) {
    delegate.addRegistrationHook(registrationHook);
  }

  private void registerWithBeanManager(InjectionContext context, Statement valueRef) {
    if (InjectUtil.checkIfTypeNeedsAddingToBeanStore(context, this)) {
      QualifyingMetadata md = delegate.getQualifyingMetadata();
      if (md == null) {
        md = context.getProcessingContext().getQualifyingMetadataFactory().createDefaultMetadata();
      }

      context.getProcessingContext().appendToEnd(
              Stmt.loadVariable(context.getProcessingContext().getContextVariableReference())
                      .invoke("addBean", type, Refs.get(delegate.getCreationalCallbackVarName()),
                              isSingleton() ? valueRef : null , md.render()));
    }
  }

  @Override
  public boolean isAlternative() {
    return delegate.isAlternative();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
