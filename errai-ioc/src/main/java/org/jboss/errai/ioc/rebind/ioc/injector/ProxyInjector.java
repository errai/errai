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

import org.jboss.errai.codegen.framework.InnerClass;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.ProxyMaker;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.Scope;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.client.container.ProxyResolver;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.framework.util.Stmt.declareVariable;
import static org.jboss.errai.codegen.framework.util.Stmt.loadVariable;
import static org.jboss.errai.codegen.framework.util.Stmt.newObject;

/**
 * @author Mike Brock
 */
public class ProxyInjector extends Injector {
  private boolean proxied;
  private final String varName = InjectUtil.getNewInjectorName();

  private Statement proxyStatement;
  private BlockBuilder<AnonymousClassStructureBuilder> proxyResolverBody;

  private final MetaClass proxiedType;
  private final BuildMetaClass proxyClass;

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
  public Statement getBeanInstance(InjectionContext injectContext, InjectableInstance injectableInstance) {
    if (!isInjected()) {
      IOCProcessingContext pCtx = injectContext.getProcessingContext();

      pCtx.append(declareVariable(proxyClass).asFinal().named(varName).initializeWith(newObject(proxyClass)));

      MetaClass proxyResolverRef = parameterizedAs(ProxyResolver.class, typeParametersOf(proxiedType));

      proxyResolverBody = newObject(proxyResolverRef)
              .extend().publicOverridesMethod("resolve", Parameter.of(proxiedType, "obj"));

      Statement proxyResolver = proxyResolverBody.append(loadVariable(varName)
              .invoke(ProxyMaker.PROXY_BIND_METHOD, Refs.get("obj"))).finish().finish();

      proxyResolverBody.append(Stmt.loadVariable("context").invoke("addProxyReference", Refs.get(varName), Refs.get("obj")));

      pCtx.append(loadVariable("context").invoke("addUnresolvedProxy", proxyResolver,
              proxiedType, qualifyingMetadata.getQualifiers()));

      injected = true;

    }
    return !proxied ? loadVariable(varName) : proxyStatement;
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

  public void addProxyCloseStatement(Statement statement) {
    proxyResolverBody.append(statement);
  }
}
