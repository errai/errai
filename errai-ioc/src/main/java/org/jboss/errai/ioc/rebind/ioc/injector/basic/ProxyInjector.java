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

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;
import static org.jboss.errai.codegen.util.Stmt.newObject;

import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.ProxyMaker;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.BlockBuilderImpl;
import org.jboss.errai.codegen.builder.impl.Scope;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ioc.client.container.ProxyResolver;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ProxyInjector extends AbstractInjector {
  private final String varName;
  private final List<Statement> closeStatements;
  private final MetaClass proxiedType;
  private final BuildMetaClass proxyClass;

  public ProxyInjector(final IOCProcessingContext context,
                       final MetaClass proxiedType,
                       final QualifyingMetadata metadata) {

    Assert.notNull(proxiedType);
    Assert.notNull(metadata);

    this.proxiedType = proxiedType;
    this.varName = InjectUtil.getNewInjectorName() + "_proxy";
    this.qualifyingMetadata = AbstractInjector.getMetadataWithAny(metadata);
    final String proxyClassName = proxiedType.getName() + "_" + varName;

    this.closeStatements = new ArrayList<Statement>();

    this.proxyClass = ProxyMaker.makeProxy(proxyClassName, proxiedType, context.isGwtTarget() ? "jsni" : "reflection");
    this.proxyClass.setStatic(true);
    this.proxyClass.setScope(Scope.Package);

    context.getBootstrapClass()
            .addInnerClass(new InnerClass(proxyClass));
  }

  @Override
  public void renderProvider(InjectableInstance injectableInstance) {
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {
    final IOCProcessingContext pCtx = injectableInstance.getInjectionContext().getProcessingContext();

    pCtx.append(Stmt.declareFinalVariable(varName, proxyClass, newObject(proxyClass)));

    final MetaClass proxyResolverRef = parameterizedAs(ProxyResolver.class, typeParametersOf(proxiedType));

    final BlockBuilder<AnonymousClassStructureBuilder> proxyResolverBody = newObject(proxyResolverRef)
            .extend().publicOverridesMethod("resolve", Parameter.of(proxiedType, "obj"));

    final Statement proxyResolver = proxyResolverBody._(
        ProxyMaker.closeProxy(Refs.get(varName), Refs.get("obj"))

    ).finish().finish();

    proxyResolverBody._(Stmt.loadVariable("context").invoke("addProxyReference", Refs.get(varName), Refs.get("obj")));

    pCtx.append(loadVariable("context").invoke("addUnresolvedProxy", proxyResolver,
            proxiedType, qualifyingMetadata.getQualifiers()));

    for (final Statement statement : closeStatements) {
      proxyResolverBody.append(statement);
    }

    setRendered(true);
    return loadVariable(varName);
  }

  @Override
  public String getInstanceVarName() {
    return varName;
  }

  @Override
  public MetaClass getInjectedType() {
    return proxiedType;
  }

  public void addProxyCloseStatement(final Statement statement) {
    closeStatements.add(statement);
  }

  public BlockBuilder getProxyResolverBlockBuilder() {
    return new BlockBuilderImpl() {
      @Override
      public BlockBuilder append(final Statement stmt) {
        closeStatements.add(stmt);
        return this;
      }
    };
  }
}
