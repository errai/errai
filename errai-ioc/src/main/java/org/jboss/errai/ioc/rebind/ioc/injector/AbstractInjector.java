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

import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import javax.enterprise.inject.New;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractInjector implements Injector {
  protected QualifyingMetadata qualifyingMetadata;
  protected MetaParameterizedType qualifyingTypeInformation;
  protected String postInitCallbackVar = null;
  protected String preDestroyCallbackVar = null;
  protected String creationalCallbackVarName = null;

  protected boolean enabled = true;
  protected boolean testmock;
  protected boolean alternative;
  private boolean created;
  private boolean rendered;
  protected boolean singleton;
  protected boolean replaceable;
  protected boolean provider;
  protected boolean basic;

  protected MetaClass enclosingType;

  protected String beanName;

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

  public void setCreated(final boolean created) {
    this.created = created;
  }

  public void setRendered(final boolean rendered) {
    this.rendered = rendered;
  }

  public void setReplaceable(final boolean replaceable) {
    this.replaceable = replaceable;
  }

  @Override
  public String getPostInitCallbackVar() {
    return postInitCallbackVar;
  }

  @Override
  public void setPostInitCallbackVar(final String var) {
    this.postInitCallbackVar = var;
  }

  @Override
  public String getPreDestroyCallbackVar() {
    return preDestroyCallbackVar;
  }

  @Override
  public void setPreDestroyCallbackVar(final String preDestroyCallbackVar) {
    this.preDestroyCallbackVar = preDestroyCallbackVar;
  }

  @Override
  public String getCreationalCallbackVarName() {
    return creationalCallbackVarName;
  }

  public void setCreationalCallbackVarName(final String creationalCallbackVarName) {
    this.creationalCallbackVarName = creationalCallbackVarName;
  }

  @Override
  public boolean matches(final MetaParameterizedType parameterizedType, final QualifyingMetadata qualifyingMetadata) {
    boolean parmTypesSatisfied = true;
    if (parameterizedType != null) {
      parmTypesSatisfied = parameterizedType.isAssignableFrom(getQualifyingTypeInformation());
    }

    final boolean metaDataSatisfied = getQualifyingMetadata() == null
        || getQualifyingMetadata().doesSatisfy(qualifyingMetadata);

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

  public void setQualifyingTypeInformation(final MetaParameterizedType qualifyingTypeInformation) {
    this.qualifyingTypeInformation = qualifyingTypeInformation;
  }

  @Override
  public String getInstanceVarName() {
    throw new UnsupportedOperationException("this injector type does have any variable name associated with it");
  }

  static class RegisterCache {
    private final InjectionContext _injectionContextForRegister;
    private final Statement _valueRefForRegister;

    RegisterCache(final InjectionContext _injectionContextForRegister, final Statement _valueRefForRegister) {
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
  public void addRegistrationHook(final RegistrationHook registrationHook) {
    if (_registerCache == null)
      registrationHooks.add(registrationHook);
    else
      registrationHook.onRegister(_registerCache.getInjectionContextForRegister(), _registerCache.getValueRefForRegister());
  }

  private Runnable _disableHook;

  public void registerWithBeanManager(final InjectionContext context,
                                      final Statement valueRef) {
    if (!isEnabled()) {
      return;
    }

    if (InjectUtil.checkIfTypeNeedsAddingToBeanStore(context, this)) {
      _registerCache = new RegisterCache(context, valueRef);
      final ContextualStatementBuilder statement;
      if (beanName == null) {
        statement = loadVariable(context.getProcessingContext().getContextVariableReference())
            .invoke("addBean", getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                isSingleton() ? valueRef : null, qualifyingMetadata.render());
      }
      else {
        statement = loadVariable(context.getProcessingContext().getContextVariableReference())
            .invoke("addBean", getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                isSingleton() ? valueRef : null, qualifyingMetadata.render(), beanName);
      }

      context.getProcessingContext().appendToEnd(statement);

      _disableHook = new Runnable() {
        @Override
        public void run() {
          context.getProcessingContext().getAppendToEnd().remove(statement);
        }
      };

      for (final RegistrationHook hook : registrationHooks) {
        hook.onRegister(context, valueRef);
      }
    }
  }

  @Override
  public String toString() {
    return this.getClass().getName() + ":" + getInjectedType().getFullyQualifiedName() + " " + getQualifyingMetadata();
  }

  @Override
  public String getBeanName() {
    return beanName;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!(this.enabled = enabled) && _disableHook != null) {
      _disableHook.run();
    }
  }

  protected Set<Annotation> makeSpecialized(final InjectionContext context) {
    final MetaClass type = getInjectedType();

    if (type.getSuperClass().getFullyQualifiedName().equals(Object.class.getName())) {
      throw new InjectionFailure("the specialized bean " + type.getFullyQualifiedName() + " must directly inherit "
          + "from another bean");
    }

    final Set<Annotation> qualifiers = new HashSet<Annotation>();

    MetaClass cls = type;
    while ((cls = cls.getSuperClass()) != null && !cls.getFullyQualifiedName().equals(Object.class.getName())) {
      if (!context.hasInjectorForType(cls)) {
        context.addType(cls);
      }

      final Injector inj = context.getInjector(cls);

      if (this.beanName == null) {
        this.beanName = inj.getBeanName();
      }

      inj.setEnabled(false);
      qualifiers.addAll(Arrays.asList(inj.getQualifyingMetadata().getQualifiers()));
    }

    return qualifiers;
  }

  protected static boolean hasNewQualifier(final InjectableInstance instance) {
    if (instance != null) {
      for (final Annotation annotation : instance.getQualifiers()) {
        if (annotation.annotationType().equals(New.class)) return true;
      }
    }
    return false;
  }
}

