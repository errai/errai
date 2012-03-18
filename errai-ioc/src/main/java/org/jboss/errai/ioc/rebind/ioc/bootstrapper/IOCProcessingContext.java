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
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.VariableReference;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.metadata.JSR330QualifyingMetadataFactory;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadataFactory;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TypeDiscoveryListener;

import java.lang.annotation.Annotation;
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
  protected Context context;
  protected BuildMetaClass bootstrapClass;

  protected Stack<BlockBuilder<?>> blockBuilder;

  protected Collection<String> packages;
  protected List<Statement> appendToEnd;
  
  protected List<Statement> staticInstantiationStatements;
  protected List<Statement> staticPostConstructStatements;
  protected List<TypeDiscoveryListener> typeDiscoveryListeners;
  
  protected List<Injector> toInstantiate;

  protected Set<MetaClass> discovered = new HashSet<MetaClass>();
  
  protected TreeLogger treeLogger;
  protected GeneratorContext generatorContext;

  protected SourceWriter writer;

  protected Variable contextVariable = Variable.create("injContext", InterfaceInjectionContext.class);

  protected QualifyingMetadataFactory qualifyingMetadataFactory = new JSR330QualifyingMetadataFactory();

  protected Set<Class<? extends Annotation>> singletonScopes;

  public IOCProcessingContext(TreeLogger treeLogger,
                              GeneratorContext generatorContext,
                              SourceWriter writer,
                              Context context,
                              BuildMetaClass bootstrapClass,
                              BlockBuilder<?> blockBuilder) {
    this.treeLogger = treeLogger;
    this.generatorContext = generatorContext;
    this.writer = writer;
    this.context = context;
    this.bootstrapClass = bootstrapClass;

    this.blockBuilder = new Stack<BlockBuilder<?>>();
    this.blockBuilder.push(blockBuilder);

    this.appendToEnd = new ArrayList<Statement>();
    this.staticInstantiationStatements = new ArrayList<Statement>();
    this.staticPostConstructStatements = new ArrayList<Statement>();
    this.typeDiscoveryListeners = new ArrayList<TypeDiscoveryListener>();
    this.singletonScopes = new HashSet<Class<? extends Annotation>>();
    this.toInstantiate = new ArrayList<Injector>();
  }

  public void addSingletonScopeAnnotation(Class<? extends Annotation> annotation) {
    this.singletonScopes.add(annotation);
  }

  public boolean isSingletonScope(Class<? extends Annotation> annotation) {
    return this.singletonScopes.contains(annotation);
  }

  public boolean isSingletonScope(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (isSingletonScope(a.annotationType())) return true;
    }
    return false;
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
  }

  public void appendToEnd(Statement statement) {
    appendToEnd.add(statement);
  }


  public List<Statement> getAppendToEnd() {
    return Collections.unmodifiableList(appendToEnd);
  }

  public List<Statement> getStaticInstantiationStatements() {
    return Collections.unmodifiableList(staticInstantiationStatements);
  }

  public List<Statement> getStaticPostConstructStatements() {
    return Collections.unmodifiableList(staticPostConstructStatements);
  }

  public List<Injector> getToInstantiate() {
    return Collections.unmodifiableList(toInstantiate);
  }

  public BuildMetaClass getBootstrapClass() {
    return bootstrapClass;
  }

  public Context getContext() {
    return context;
  }

  public void setPackages(Collection<String> packages) {
    this.packages = packages;
  }

  public Collection<String> getPackages() {
    return packages;
  }

  public TreeLogger getTreeLogger() {
    return treeLogger;
  }

  public GeneratorContext getGeneratorContext() {
    return generatorContext;
  }

  public Variable getContextVariable() {
    return contextVariable;
  }

  public VariableReference getContextVariableReference() {
    return contextVariable.getReference();
  }

  public QualifyingMetadataFactory getQualifyingMetadataFactory() {
    return qualifyingMetadataFactory;
  }

  public void setQualifyingMetadataFactory(QualifyingMetadataFactory qualifyingMetadataFactory) {
    this.qualifyingMetadataFactory = qualifyingMetadataFactory;
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
