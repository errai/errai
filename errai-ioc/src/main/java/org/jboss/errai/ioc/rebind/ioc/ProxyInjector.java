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

import org.jboss.errai.codegen.framework.InnerClass;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.ProxyMaker;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.impl.Scope;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.client.container.ProxyResolver;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;

/**
 * @author Mike Brock
 */
public class ProxyInjector extends Injector {
  private boolean proxied;
  private final String varName = InjectUtil.getNewVarName();
  private Statement proxyStatement;
  private final MetaClass proxiedType;
  private final BuildMetaClass proxyClass;
  private boolean isInjected;

  public ProxyInjector(IOCProcessingContext context, MetaClass proxiedType, QualifyingMetadata metadata) {
    this.proxiedType = proxiedType;
    this.qualifyingMetadata = metadata;
    String proxyClassName = proxiedType.getFullyQualifiedName().replaceAll("\\.", "_") + "_" + varName;
    this.proxyClass = ProxyMaker.makeProxy(proxyClassName, proxiedType);
    this.proxyClass.setStatic(true);
    this.proxyClass.setScope(Scope.Package);

    context.getBootstrapClass()
            .addInnerClass(new InnerClass(proxyClass));
  }

  @Override
  public Statement instantiateOnly(InjectionContext injectContext, InjectableInstance injectableInstance) {
    return getType(injectContext, injectableInstance);
  }

  @Override
  public Statement getType(InjectionContext injectContext, InjectableInstance injectableInstance) {
    if (!isInjected()) {
      IOCProcessingContext pCtx = injectContext.getProcessingContext();

      pCtx.append(Stmt.declareVariable(proxyClass).asFinal().named(varName).initializeWith(Stmt.newObject(proxyClass)));

      MetaClass proxyResolverRef = MetaClassFactory.parameterizedAs(ProxyResolver.class,
              MetaClassFactory.typeParametersOf(proxiedType));

      Statement proxyResolver = Stmt.newObject(proxyResolverRef)
              .extend().publicOverridesMethod("resolve", Parameter.of(proxiedType, "obj"))
              .append(Stmt.loadVariable(varName).invoke(ProxyMaker.PROXY_BIND_METHOD, Refs.get("obj"))).finish().finish();


      pCtx.append(Stmt.loadVariable("context").invoke("addUnresolvedProxy", proxyResolver,
              proxiedType, qualifyingMetadata.getQualifiers()));
      isInjected = true;

    }
    return !proxied ? Stmt.loadVariable(varName) : proxyStatement;
  }

  @Override
  public boolean isInjected() {
    return isInjected;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  @Override
  public boolean isPseudo() {
    return false;
  }

  @Override
  public String getVarName() {
    return varName;
  }

  @Override
  public MetaClass getInjectedType() {
    return proxiedType;
  }

  public boolean isProxied() {
    return proxied;
  }

  public void setProxied(boolean proxied) {
    this.proxied = proxied;
  }

  public void setProxyStatement(Statement proxyStatement) {
    this.proxyStatement = proxyStatement;
  }

  public BuildMetaClass getProxyClass() {
    return proxyClass;
  }
}
