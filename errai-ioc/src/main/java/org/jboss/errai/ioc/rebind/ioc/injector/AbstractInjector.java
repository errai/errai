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
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.errai.codegen.framework.util.Stmt.loadVariable;

public abstract class AbstractInjector implements Injector {
  protected boolean useBeanManager = !Boolean.getBoolean("errai.ioc.no_bean_manager");

  protected QualifyingMetadata qualifyingMetadata;
  protected MetaParameterizedType qualifyingTypeInformation;
  protected String postInitCallbackVar = null;
  protected String preDestroyCallbackVar = null;
  protected String creationalCallbackVarName = null;

  protected boolean testmock;
  protected boolean alternative;
  protected boolean injected;
  protected boolean singleton;
  protected boolean replaceable;
  protected boolean provider;

  protected MetaClass enclosingType;

  private List<RegistrationHook> registrationHooks = new ArrayList<RegistrationHook>();

  @Override
  public Statement getBeanInstance(InjectableInstance injectableInstance) {
    return getBeanInstance(injectableInstance.getInjectionContext(), injectableInstance);
  }

  public boolean isTestmock() {
    return testmock;
  }

  @Override
  public boolean isAlternative() {
    return alternative;
  }

  @Override
  public boolean isInjected() {
    return injected;
  }

  @Override
  public boolean isSingleton() {
    return singleton;
  }

  @Override
  public boolean isDependent() {
    return !singleton;
  }

  @Override
  public boolean isPseudo() {
    return replaceable;
  }

  @Override
  public boolean isProvider() {
    return provider;
  }

  @Override
  public MetaClass getEnclosingType() {
    return enclosingType;
  }

  public void setInjected(boolean injected) {
    this.injected = injected;
  }

  public void setReplaceable(boolean replaceable) {
    this.replaceable = replaceable;
  }

  @Override
  public String getPostInitCallbackVar() {
    return postInitCallbackVar;
  }

  @Override
  public void setPostInitCallbackVar(String var) {
    this.postInitCallbackVar = var;
  }

  @Override
  public String getPreDestroyCallbackVar() {
    return preDestroyCallbackVar;
  }

  @Override
  public void setPreDestroyCallbackVar(String preDestroyCallbackVar) {
    this.preDestroyCallbackVar = preDestroyCallbackVar;
  }

  public String getCreationalCallbackVarName() {
    return creationalCallbackVarName;
  }

  public void setCreationalCallbackVarName(String creationalCallbackVarName) {
    this.creationalCallbackVarName = creationalCallbackVarName;
  }

  @Override
  public boolean metadataMatches(Injector injector) {
    boolean meta = (injector == null && qualifyingMetadata == null) ||
            (injector != null && injector.getQualifyingMetadata() != null
                    && qualifyingMetadata != null
                    && injector.getQualifyingMetadata().doesSatisfy(qualifyingMetadata));

    return meta && (qualifyingTypeInformation == null && (injector != null && injector.getQualifyingTypeInformation() ==
            null)
            || !(qualifyingTypeInformation == null || (injector != null && injector.getQualifyingTypeInformation() ==
            null))
            && injector != null && qualifyingTypeInformation.isAssignableFrom(injector.getQualifyingTypeInformation()));
  }

  @Override
  public boolean matches(MetaParameterizedType parameterizedType, QualifyingMetadata qualifyingMetadata) {
    boolean parmTypesSatisfied = true;
    if (parameterizedType != null) {
      parmTypesSatisfied = parameterizedType.isAssignableFrom(getQualifyingTypeInformation());
    }

    boolean metaDataSatisfied = getQualifyingMetadata() == null || getQualifyingMetadata().doesSatisfy
            (qualifyingMetadata);

    return parmTypesSatisfied && metaDataSatisfied;
  }

  @Override
  public QualifyingMetadata getQualifyingMetadata() {
    return qualifyingMetadata;
  }

  @Override
  public MetaParameterizedType getQualifyingTypeInformation() {
    return qualifyingTypeInformation;
  }

  @Override
  public void setQualifyingTypeInformation(MetaParameterizedType qualifyingTypeInformation) {
    this.qualifyingTypeInformation = qualifyingTypeInformation;
  }


  static class RegisterCache {
    private final InjectionContext _injectionContextForRegister;
    private final Statement _valueRefForRegister;

    RegisterCache(InjectionContext _injectionContextForRegister, Statement _valueRefForRegister) {
      this._injectionContextForRegister = _injectionContextForRegister;
      this._valueRefForRegister = _valueRefForRegister;
    }

    public InjectionContext getInjectionContextForRegister() {
      return _injectionContextForRegister;
    }

    public Statement getValueRefForRegister() {
      return _valueRefForRegister;
    }
  }

  private RegisterCache _registerCache;

  @Override
  public void addRegistrationHook(RegistrationHook registrationHook) {
    if (_registerCache == null)
      registrationHooks.add(registrationHook);
    else
      registrationHook.onRegister(_registerCache.getInjectionContextForRegister(), _registerCache.getValueRefForRegister());
  }

  public void registerWithBeanManager(InjectionContext context, Statement valueRef) {
    if (InjectUtil.checkIfTypeNeedsAddingToBeanStore(context, this)) {
      _registerCache = new RegisterCache(context, valueRef);

      context.getProcessingContext().appendToEnd(
              loadVariable(context.getProcessingContext().getContextVariableReference())
                      .invoke("addBean", getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                              isSingleton() ? valueRef : null, qualifyingMetadata.render())
      );

      for (RegistrationHook hook : registrationHooks) {
        hook.onRegister(context, valueRef);
      }
    }
  }

  public String toString() {
    return this.getClass().getName() + ":" + getInjectedType().getFullyQualifiedName() + " " + getQualifyingMetadata();
  }
}

