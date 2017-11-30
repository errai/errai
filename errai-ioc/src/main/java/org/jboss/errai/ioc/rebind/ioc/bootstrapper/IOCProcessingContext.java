/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import com.google.gwt.core.ext.GeneratorContext;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.config.MetaClassFinder;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.common.apt.ResourceFilesFinder;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;

import java.util.Stack;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCProcessingContext {

  protected final BuildMetaClass bootstrapClass;
  protected final ClassStructureBuilder bootstrapBuilder;
  protected final Stack<BlockBuilder<?>> blockBuilder;
  protected final GeneratorContext generatorContext;
  protected final MetaClassFinder metaClassFinder;
  protected final ErraiConfiguration erraiConfiguration;
  protected final ResourceFilesFinder resourceFilesFinder;

  private IOCProcessingContext(final Builder builder) {
    this.generatorContext = builder.generatorContext;
    this.bootstrapClass = builder.bootstrapClassInstance;
    this.bootstrapBuilder = builder.bootstrapBuilder;
    this.metaClassFinder = builder.metaClassFinder;
    this.erraiConfiguration = builder.erraiConfiguration;
    this.resourceFilesFinder = builder.resourcesFilesFinder;

    this.blockBuilder = new Stack<>();
    this.blockBuilder.push(builder.blockBuilder);
  }

  public MetaClass buildFactoryMetaClass(final Injectable injectable) {
    final String factoryName = erraiConfiguration.app().namespace() + injectable.getFactoryName();
    final MetaClass typeCreatedByFactory = injectable.getInjectedType();
    final BuildMetaClass factoryMetaClass = ClassBuilder.define(factoryName,
            parameterizedAs(Factory.class, typeParametersOf(typeCreatedByFactory)))
            .publicScope()
            .abstractClass()
            .body()
            .getClassDefinition();

    if (!erraiConfiguration.app().isAptEnvironment()) {
      getBootstrapBuilder().declaresInnerClass(new InnerClass(factoryMetaClass));
    }

    return factoryMetaClass;
  }

  public static class Builder {
    private GeneratorContext generatorContext;
    private BuildMetaClass bootstrapClassInstance;
    private ClassStructureBuilder bootstrapBuilder;
    private MetaClassFinder metaClassFinder;
    private BlockBuilder<?> blockBuilder;
    private ErraiConfiguration erraiConfiguration;
    private ResourceFilesFinder resourcesFilesFinder;

    public static Builder create() {
      return new Builder();
    }

    public Builder generatorContext(final GeneratorContext generatorContext) {
      this.generatorContext = generatorContext;
      return this;
    }

    public Builder metaClassFinder(final MetaClassFinder metaClassFinder) {
      this.metaClassFinder = metaClassFinder;
      return this;
    }

    public Builder bootstrapClassInstance(final BuildMetaClass bootstrapClassInstance) {
      this.bootstrapClassInstance = bootstrapClassInstance;
      return this;
    }

    public Builder bootstrapBuilder(final ClassStructureBuilder classStructureBuilder) {
      this.bootstrapBuilder = classStructureBuilder;
      return this;
    }

    public Builder blockBuilder(final BlockBuilder<?> blockBuilder) {
      this.blockBuilder = blockBuilder;
      return this;
    }

    public Builder erraiConfiguration(final ErraiConfiguration erraiConfiguration) {
      this.erraiConfiguration = erraiConfiguration;
      return this;
    }

    public Builder resourceFilesFinder(final ResourceFilesFinder resourceFilesFinder) {
      this.resourcesFilesFinder = resourceFilesFinder;
      return this;
    }

    public IOCProcessingContext build() {
      Assert.notNull("bootstrapClassInstance cannot be null", bootstrapClassInstance);
      Assert.notNull("bootstrapBuilder cannot be null", bootstrapBuilder);
      Assert.notNull("blockBuilder cannot be null", blockBuilder);
      Assert.notNull("metaClassFinder cannot be null", metaClassFinder);
      Assert.notNull("erraiConfiguration cannot be null", erraiConfiguration);
      Assert.notNull("resourceFilesFinder cannot be null", resourcesFilesFinder);

      return new IOCProcessingContext(this);
    }
  }

  public BlockBuilder<?> getBlockBuilder() {
    return blockBuilder.peek();
  }

  public BlockBuilder<?> append(final Statement statement) {
    return getBlockBuilder().append(statement);
  }

  public void insertBefore(final Statement statement) {
     getBlockBuilder().insertBefore(statement);
  }

  public BuildMetaClass getBootstrapClass() {
    return bootstrapClass;
  }

  public ClassStructureBuilder getBootstrapBuilder() {
    return bootstrapBuilder;
  }

  public MetaClassFinder metaClassFinder() {
    return metaClassFinder;
  }

  public GeneratorContext getGeneratorContext() {
    return generatorContext;
  }

  public ErraiConfiguration erraiConfiguration() {
    return erraiConfiguration;
  }

  public ResourceFilesFinder resourceFilesFinder() {
    return resourceFilesFinder;
  }
}
