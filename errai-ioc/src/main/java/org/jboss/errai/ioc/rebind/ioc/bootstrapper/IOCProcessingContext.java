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

package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.VariableReference;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.ioc.client.BootstrapperInjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TypeDiscoveryListener;
import org.jboss.errai.ioc.rebind.ioc.metadata.JSR330QualifyingMetadataFactory;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadataFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCProcessingContext {
  protected final Collection<String> packages;

  protected final Context context;
  protected final BuildMetaClass bootstrapClass;

  protected final Stack<BlockBuilder<?>> blockBuilder;

  protected final List<Statement> appendToEnd;
  protected final List<TypeDiscoveryListener> typeDiscoveryListeners;
  protected final Set<MetaClass> discovered = new HashSet<MetaClass>();

  protected final TreeLogger treeLogger;
  protected final GeneratorContext generatorContext;

  protected final SourceWriter writer;

  protected final Variable contextVariable = Variable.create("injContext", BootstrapperInjectionContext.class);

  protected final QualifyingMetadataFactory qualifyingMetadataFactory;

  private IOCProcessingContext(final Builder builder) {
    this.treeLogger = builder.treeLogger;
    this.generatorContext = builder.generatorContext;
    this.writer = builder.sourceWriter;
    this.context = builder.context;
    this.bootstrapClass = builder.bootstrapClassInstance;

    this.blockBuilder = new Stack<BlockBuilder<?>>();
    this.blockBuilder.push(builder.blockBuilder);

    this.appendToEnd = new ArrayList<Statement>();
    this.typeDiscoveryListeners = new ArrayList<TypeDiscoveryListener>();
    this.packages = builder.packages;
    this.qualifyingMetadataFactory = builder.qualifyingMetadataFactory;
  }

  public static class Builder {
    private TreeLogger treeLogger;
    private GeneratorContext generatorContext;
    private SourceWriter sourceWriter;
    private Context context;
    private BuildMetaClass bootstrapClassInstance;
    private BlockBuilder<?> blockBuilder;
    private Collection<String> packages;
    private QualifyingMetadataFactory qualifyingMetadataFactory;

    public static Builder create() {
      return new Builder();
    }

    public Builder logger(TreeLogger treeLogger) {
      this.treeLogger = treeLogger;
      return this;
    }

    public Builder generatorContext(GeneratorContext generatorContext) {
      this.generatorContext = generatorContext;
      return this;
    }

    public Builder sourceWriter(SourceWriter sourceWriter) {
      this.sourceWriter = sourceWriter;
      return this;
    }

    public Builder context(Context context) {
      this.context = context;
      return this;
    }

    public Builder bootstrapClassInstance(BuildMetaClass bootstrapClassInstance) {
      this.bootstrapClassInstance = bootstrapClassInstance;
      return this;
    }

    public Builder blockBuilder(BlockBuilder<?> blockBuilder) {
      this.blockBuilder = blockBuilder;
      return this;
    }

    public Builder packages(Collection<String> packages) {
      this.packages = packages;
      return this;
    }

    public Builder qualifyingMetadata(QualifyingMetadataFactory qualifyingMetadataFactory) {
      this.qualifyingMetadataFactory = qualifyingMetadataFactory;
      return this;
    }

    public IOCProcessingContext build() {
      Assert.notNull("treeLogger cannot be null", treeLogger);
      Assert.notNull("sourceWriter cannot be null", sourceWriter);
      Assert.notNull("context cannot be null", context);
      Assert.notNull("bootstrapClassInstance cannot be null", bootstrapClassInstance);
      Assert.notNull("blockBuilder cannot be null", blockBuilder);
      Assert.notNull("packages cannot be null", packages);

      if (qualifyingMetadataFactory == null) {
        qualifyingMetadataFactory = new JSR330QualifyingMetadataFactory();
      }

      return new IOCProcessingContext(this);
    }
  }

  public BlockBuilder<?> getBlockBuilder() {
    return blockBuilder.peek();
  }

  public BlockBuilder<?> append(Statement statement) {
    return getBlockBuilder().append(statement);
  }

  public void globalInsertBefore(Statement statement) {
    blockBuilder.get(0).insertBefore(statement);
  }

  public BlockBuilder<?> globalAppend(Statement statement) {
    return blockBuilder.get(0).append(statement);
  }

  public void pushBlockBuilder(BlockBuilder<?> blockBuilder) {
    this.blockBuilder.push(blockBuilder);
  }

  public void popBlockBuilder() {
    this.blockBuilder.pop();

    if (this.blockBuilder.size() == 0) {
      throw new AssertionError("block builder was over popped! something is wrong.");
    }
  }

  public void appendToEnd(Statement statement) {
    appendToEnd.add(statement);
  }

  public List<Statement> getAppendToEnd() {
    return Collections.unmodifiableList(appendToEnd);
  }

  public BuildMetaClass getBootstrapClass() {
    return bootstrapClass;
  }

  public Context getContext() {
    return context;
  }

  public Collection<String> getPackages() {
    return packages;
  }

  @SuppressWarnings("UnusedDeclaration")
  public TreeLogger getTreeLogger() {
    return treeLogger;
  }

  public GeneratorContext getGeneratorContext() {
    return generatorContext;
  }

  public VariableReference getContextVariableReference() {
    return contextVariable.getReference();
  }

  public QualifyingMetadataFactory getQualifyingMetadataFactory() {
    return qualifyingMetadataFactory;
  }

  public void registerTypeDiscoveryListener(TypeDiscoveryListener discoveryListener) {
    this.typeDiscoveryListeners.add(discoveryListener);
  }

  public void handleDiscoveryOfType(InjectionPoint injectionPoint) {
    if (discovered.contains(injectionPoint.getType())) return;
    for (TypeDiscoveryListener listener : typeDiscoveryListeners) {
      listener.onDiscovery(this, injectionPoint);
    }
    discovered.add(injectionPoint.getType());
  }
}
