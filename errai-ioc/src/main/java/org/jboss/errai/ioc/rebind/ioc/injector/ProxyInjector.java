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

import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.ProxyMaker;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.Scope;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.container.ProxyResolver;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.declareVariable;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;
import static org.jboss.errai.codegen.util.Stmt.newObject;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ProxyInjector extends AbstractInjector {
  private final String varName;
  private final List<Statement> closeStatements;
  private final MetaClass proxiedType;
  private final BuildMetaClass proxyClass;

  public ProxyInjector(IOCProcessingContext context, MetaClass proxiedType, QualifyingMetadata metadata) {
    this.proxiedType = proxiedType;
    this.varName = InjectUtil.getNewInjectorName() + "_proxy";
    this.qualifyingMetadata = metadata;
    String proxyClassName = proxiedType.getFullyQualifiedName().replaceAll("\\.", "_") + "_" + varName;

    this.closeStatements = new ArrayList<Statement>();

    this.proxyClass = ProxyMaker.makeProxy(proxyClassName, proxiedType);
    this.proxyClass.setStatic(true);
    this.proxyClass.setScope(Scope.Package);

    context.getBootstrapClass()
            .addInnerClass(new InnerClass(proxyClass));
  }

  @Override
  public Statement getBeanInstance(InjectableInstance injectableInstance) {
    IOCProcessingContext pCtx = injectableInstance.getInjectionContext().getProcessingContext();

    pCtx.append(declareVariable(proxyClass).asFinal().named(varName).initializeWith(newObject(proxyClass)));

    MetaClass proxyResolverRef = parameterizedAs(ProxyResolver.class, typeParametersOf(proxiedType));

    BlockBuilder<AnonymousClassStructureBuilder> proxyResolverBody = newObject(proxyResolverRef)
            .extend().publicOverridesMethod("resolve", Parameter.of(proxiedType, "obj"));

    Statement proxyResolver = proxyResolverBody.append(loadVariable(varName)
            .invoke(ProxyMaker.PROXY_BIND_METHOD, Refs.get("obj"))).finish().finish();

    proxyResolverBody.append(Stmt.loadVariable("context").invoke("addProxyReference", Refs.get(varName), Refs.get("obj")));

    pCtx.append(loadVariable("context").invoke("addUnresolvedProxy", proxyResolver,
            proxiedType, qualifyingMetadata.getQualifiers()));

    for (Statement statement : closeStatements) {
      proxyResolverBody.append(statement);
    }

    setRendered(true);
    return loadVariable(varName);
  }

  @Override
  public String getVarName() {
    return varName;
  }

  @Override
  public MetaClass getInjectedType() {
    return proxiedType;
  }

  public void addProxyCloseStatement(Statement statement) {
    closeStatements.add(statement);
  }
}
