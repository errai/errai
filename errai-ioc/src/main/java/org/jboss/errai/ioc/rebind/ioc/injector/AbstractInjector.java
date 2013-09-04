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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.New;

import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.ProxyMaker;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.WeaveType;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.SimpleInjectionContext;
import org.jboss.errai.ioc.client.api.qualifiers.BuiltInQualifiers;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RenderingHook;
import org.jboss.errai.ioc.rebind.ioc.metadata.JSR330QualifyingMetadata;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

public abstract class AbstractInjector implements Injector {
  protected QualifyingMetadata qualifyingMetadata;
  protected MetaParameterizedType qualifyingTypeInformation;
  protected String postInitCallbackVar = null;
  protected String preDestroyCallbackVar = null;
  protected String creationalCallbackVarName = null;

  protected boolean enabled = true;
  protected boolean softDisabled = false;
  protected boolean testMock;
  protected boolean alternative;
  private boolean created;
  private boolean rendered;
  protected boolean singleton;
  protected boolean replaceable;
  protected boolean provider;
  protected boolean basic;

  protected MetaClass enclosingType;

  protected String beanName;

  private List<RegistrationHook> registrationHooks;
  private List<RenderingHook> renderingHooks;
  private List<Runnable> disablingCallbacks;
  private List<Statement> addToEndStatements;

  private Map<MetaMethod, Map<WeaveType, Collection<Statement>>> weavingStatements;
  private Map<String, ProxyMaker.ProxyProperty> proxyPropertyMap;

  protected Map<String, Object> attributes;

  @Override
  public boolean isTestMock() {
    return testMock;
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

  @Override
  public String getProxyInstanceVarName() {
    return getInstanceVarName() + "_iproxy";
  }

  @Override
  public String getInstanceVarName() {
    throw new UnsupportedOperationException("this injector type does have any variable name associated with it");
  }

  protected static class RegisterCache {
    private final InjectionContext _injectionContextForRegister;
    private final Statement _valueRefForRegister;

    public RegisterCache(final InjectionContext _injectionContextForRegister, final Statement _valueRefForRegister) {
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


  protected RegisterCache _registerCache;

  @Override
  public void addRegistrationHook(final RegistrationHook registrationHook) {
    if (registrationHooks == null) {
      registrationHooks = new ArrayList<RegistrationHook>();
    }

    if (_registerCache == null)
      registrationHooks.add(registrationHook);
    else
      registrationHook.onRegister(_registerCache.getInjectionContextForRegister(), _registerCache.getValueRefForRegister());
  }


  public void registerWithBeanManager(final InjectionContext context,
                                      Statement valueRef) {
    if (!isEnabled()) {
      return;
    }

    if (InjectUtil.checkIfTypeNeedsAddingToBeanStore(context, this)) {
      _registerCache = new RegisterCache(context, valueRef);

      if (!context.isAsync() && valueRef == null && isSingleton()) {
        valueRef = Stmt.loadStatic(SimpleInjectionContext.class, "LAZY_INIT_REF");
      }

      final ContextualStatementBuilder statement =
              loadVariable(context.getProcessingContext().getContextVariableReference())
              .invoke("addBean", getInjectedType(), getInjectedType(), Refs.get(getCreationalCallbackVarName()),
                      valueRef, qualifyingMetadata.render(), beanName, true);

      context.getProcessingContext().appendToEnd(statement);

      addDisablingHook(new Runnable() {
        @Override
        public void run() {
          context.getProcessingContext().getAppendToEnd().remove(statement);
        }
      });

      for (final RegistrationHook hook : getRegistrationHooks()) {
        hook.onRegister(context, valueRef);
      }
    }
  }

  @Override
  public void addRenderingHook(final RenderingHook renderingHook) {
    if (renderingHooks == null) {
      renderingHooks = new ArrayList<RenderingHook>();
    }

    renderingHooks.add(renderingHook);
  }

  protected void markRendered(final InjectableInstance injectableInstance) {
    for (final RenderingHook renderingHook : getRenderingHooks()) {
      renderingHook.onRender(injectableInstance);
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

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean isSoftDisabled() {
    return softDisabled;
  }

  public void setSoftDisabled(final boolean softDisabled) {
    this.softDisabled = softDisabled;
  }

  protected void disableSoftly() {
    this.enabled = false;
    this.softDisabled = true;
  }

  @Override
  public void setEnabled(final boolean enabled) {
    if (!(this.enabled = enabled)) {
      _runDisablingCallbacks();
    }
  }

  @Override
  public void addDisablingHook(final Runnable runnable) {
    if (disablingCallbacks == null) {
      disablingCallbacks = new ArrayList<Runnable>();
    }

    disablingCallbacks.add(runnable);
    if (!enabled) {
      _runDisablingCallbacks();
    }
  }

  @Override
  public boolean isRegularTypeInjector() {
    return false;
  }

  private void _runDisablingCallbacks() {
    for (final Runnable run : getDisablingCallbacks()) {
      run.run();
    }
  }

  protected static boolean hasNewQualifier(final InjectableInstance instance) {
    if (instance != null) {
      for (final Annotation annotation : instance.getQualifiers()) {
        if (annotation.annotationType().equals(New.class)) return true;
      }
    }
    return false;
  }

  @Override
  public MetaClass getConcreteInjectedType() {
    return getInjectedType();
  }

  protected List<RegistrationHook> getRegistrationHooks() {
    if (registrationHooks == null) {
      return Collections.emptyList();
    }
    return registrationHooks;
  }

  protected List<RenderingHook> getRenderingHooks() {
    if (renderingHooks == null) {
      return Collections.emptyList();
    }
    return renderingHooks;
  }

  protected List<Runnable> getDisablingCallbacks() {
    if (disablingCallbacks == null) {
      return Collections.emptyList();
    }

    return disablingCallbacks;
  }

  @Override
  public void setAttribute(String name, Object value) {
    if (attributes == null) {
      attributes = new HashMap<String, Object>();
    }
    attributes.put(name, value);
  }

  @Override
  public Object getAttribute(String name) {
    if (attributes == null) {
      return null;
    }
    else {
      return attributes.get(name);
    }
  }

  @Override
  public boolean hasAttribute(String name) {
    return attributes != null && attributes.containsKey(name);
  }

  public Map<MetaMethod, Map<WeaveType, Collection<Statement>>> getWeavingStatements() {
    if (weavingStatements == null) {
      return Collections.emptyMap();
    }

    return weavingStatements;
  }

  public Map<MetaMethod, Map<WeaveType, Collection<Statement>>> getWeavingStatementsMap() {
    if (weavingStatements == null) {
      return Collections.emptyMap();
    }
    else {
      return weavingStatements;
    }
  }

  @Override
  public Map<String, ProxyMaker.ProxyProperty> getProxyPropertyMap() {
    if (proxyPropertyMap == null) {
      return Collections.emptyMap();
    }
    else {
      return proxyPropertyMap;
    }
  }

  public List<Statement> getAddToEndStatements() {
    if (addToEndStatements == null) {
      return Collections.emptyList();
    }
    else {
      return addToEndStatements;
    }
  }

  /**
   * Add a statement to the end of the bean injector code. Statements added here will be rendered after all other
   * binding activity and right before the injector returns the bean reference.
   *
   * @param statement
   */
  @Override
  public void addStatementToEndOfInjector(Statement statement) {
    if (addToEndStatements == null) {
      addToEndStatements = new ArrayList<Statement>();
    }
    addToEndStatements.add(statement);
  }


  @Override
  public boolean isProxied() {
    return !getWeavingStatements().isEmpty();
  }

  public List<Statement> createProxyDeclaration(InjectionContext context) {
    return createProxyDeclaration(context, Refs.get(getInstanceVarName()));
  }

  static final String RENDERED_PROXIES = "^RenderedProxies";


  public List<Statement> createProxyDeclaration(InjectionContext context, Statement beanRef) {
    if (isProxied()) {
      final BuildMetaClass type = ProxyMaker.makeProxy(
          getInjectedType(),
          context.getProcessingContext().isGwtTarget() ? "jsni" : "reflection",
          getProxyPropertyMap(),
          getWeavingStatementsMap()
      );

      if (!context.hasAttribute(RENDERED_PROXIES)) {
        context.setAttribute(RENDERED_PROXIES, new HashSet<String>());

      }
      final Set<String> proxies = (Set<String>) context.getAttribute(RENDERED_PROXIES);
      if (!proxies.contains(type.getCanonicalName())) {
        context.getProcessingContext().getBootstrapClass()
            .addInnerClass(new InnerClass(type));
        proxies.add(type.getCanonicalName());
      }

      final List<Statement> proxyCloseStmts = new ArrayList<Statement>();

      proxyCloseStmts.add(Stmt.declareFinalVariable(
          getProxyInstanceVarName(),
          type,
          Stmt.newObject(type)
      ));

      proxyCloseStmts.add(ProxyMaker.closeProxy(Refs.get(getProxyInstanceVarName()), beanRef));

      proxyCloseStmts.addAll(ProxyMaker.createAllPropertyBindings(Refs.get(getProxyInstanceVarName()), getProxyPropertyMap()));

      return proxyCloseStmts;
    }
    else {
      return Collections.emptyList();
    }
  }

  private void addWeavedStatement(final MetaMethod method, final Statement statement, WeaveType type) {
    if (weavingStatements == null) {
      weavingStatements = new HashMap<MetaMethod, Map<WeaveType, Collection<Statement>>>();
    }

    Map<WeaveType, Collection<Statement>> weaveTypeListMap = weavingStatements.get(method);
    if (weaveTypeListMap == null) {
      weavingStatements.put(method, weaveTypeListMap = new HashMap<WeaveType, Collection<Statement>>());
    }

    Collection<Statement> statements = weaveTypeListMap.get(type);
    if (statements == null) {
      weaveTypeListMap.put(type, statements = new ArrayList<Statement>());
    }

    statements.add(statement);
  }

  @Override
  public void addInvokeAround(final MetaMethod method, final Statement statement) {
    addWeavedStatement(method, statement, WeaveType.AroundInvoke);
  }

  @Override
  public void addInvokeBefore(final MetaMethod method, final Statement statement) {
    addWeavedStatement(method, statement, WeaveType.BeforeInvoke);
  }

  @Override
  public void addInvokeAfter(final MetaMethod method, Statement statement) {
    addWeavedStatement(method, statement, WeaveType.AfterInvoke);
  }


  @Override
  public ProxyMaker.ProxyProperty addProxyProperty(String propertyName, Class type, Statement statement) {
    return addProxyProperty(propertyName, MetaClassFactory.get(type), statement);
  }

  @Override
  public ProxyMaker.ProxyProperty addProxyProperty(String propertyName, MetaClass type, Statement statementReference) {
    if (proxyPropertyMap == null) {
      proxyPropertyMap = new HashMap<String, ProxyMaker.ProxyProperty>();
    }
    final ProxyMaker.ProxyProperty value = new ProxyMaker.ProxyProperty(propertyName, type, statementReference);
    proxyPropertyMap.put(propertyName, value);
    return value;
  }

  @Override
  public void updateProxies() {
  }

  public static QualifyingMetadata getMetadataWithAny(QualifyingMetadata metadata) {
    if (metadata == null)
      return JSR330QualifyingMetadata.createFromAnnotations(new Annotation[] {BuiltInQualifiers.ANY_INSTANCE});
    
    Annotation[] qualifiers = new Annotation[metadata.getQualifiers().length+1];
    
    for (int i = 0; i < metadata.getQualifiers().length; i++) {
      qualifiers[i] = metadata.getQualifiers()[i];
    }
    
    qualifiers[qualifiers.length-1] = BuiltInQualifiers.ANY_INSTANCE;
    
    return JSR330QualifyingMetadata.createFromAnnotations(qualifiers);
  }
}

