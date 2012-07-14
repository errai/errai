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

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.errai.codegen.util.Stmt.loadVariable;

public abstract class AbstractInjector implements Injector {
  protected QualifyingMetadata qualifyingMetadata;
  protected MetaParameterizedType qualifyingTypeInformation;
  protected String postInitCallbackVar = null;
  protected String preDestroyCallbackVar = null;
  protected String creationalCallbackVarName = null;

  protected boolean testmock;
  protected boolean alternative;
  private boolean created;
  private boolean rendered;
  protected boolean singleton;
  protected boolean replaceable;
  protected boolean provider;
  protected boolean basic;

  protected MetaClass enclosingType;

  protected final List<RegistrationHook> registrationHooks = new ArrayList<RegistrationHook>();

  @Override
  public boolean isTestmock() {
    return testmock;
  }

  @Override
  public boolean isAlternative() {
    return alternative;
  }

  @Override
  public boolean isRendered() {
    return rendered;
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
  public boolean isStatic() {
    return false;
  }

  @Override
  public MetaClass getEnclosingType() {
    return enclosingType;
  }

  @Override
  public boolean isCreated() {
    return created;
  }

  public void setCreated(boolean created) {
    this.created = created;
  }

  public void setRendered(boolean rendered) {
    this.rendered = rendered;
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

  @Override
  public String getCreationalCallbackVarName() {
    return creationalCallbackVarName;
  }

  public void setCreationalCallbackVarName(String creationalCallbackVarName) {
    this.creationalCallbackVarName = creationalCallbackVarName;
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

  public void setQualifyingTypeInformation(MetaParameterizedType qualifyingTypeInformation) {
    this.qualifyingTypeInformation = qualifyingTypeInformation;
  }

  @Override
  public String getVarName() {
    throw new UnsupportedOperationException("this injector type does have any variable name associated with it");
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

      context.getProcessingContext().globalAppend(
              loadVariable(context.getProcessingContext().getContextVariableReference())
                      .invoke("addBean", getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                              isSingleton() ? valueRef : null, qualifyingMetadata.render())
      );

      for (RegistrationHook hook : registrationHooks) {
        hook.onRegister(context, valueRef);
      }
    }
  }

  @Override
  public String toString() {
    return this.getClass().getName() + ":" + getInjectedType().getFullyQualifiedName() + " " + getQualifyingMetadata();
  }
}

