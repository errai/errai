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

package org.jboss.errai.ioc.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.JSR330QualifyingMetadataFactory;
import org.jboss.errai.ioc.rebind.ioc.QualifyingMetadataFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCProcessingContext  {
  protected Context context;
  protected MetaClass bootstrapClass;
  protected BlockBuilder<?> blockBuilder;
  protected String packageFilter;

  protected TreeLogger treeLogger;
  protected GeneratorContext generatorContext;

  protected SourceWriter writer;
  protected TypeOracle oracle;

  protected QualifyingMetadataFactory qualifyingMetadataFactory = new JSR330QualifyingMetadataFactory();

  public IOCProcessingContext(TreeLogger treeLogger,
                              GeneratorContext generatorContext,
                              SourceWriter writer,
                              TypeOracle oracle,
                              Context context,
                              MetaClass bootstrapClass,
                              BlockBuilder<?> blockBuilder) {
    this.treeLogger = treeLogger;
    this.generatorContext = generatorContext;
    this.writer = writer;
    this.context = context;
    this.bootstrapClass = bootstrapClass;
    this.blockBuilder = blockBuilder;
  }


  public BlockBuilder<?> getBlockBuilder() {
    return blockBuilder;
  }

  public BlockBuilder<?> append(Statement statement) {
    return blockBuilder.append(statement);
  }

  public MetaClass getBootstrapClass() {
    return bootstrapClass;
  }

  public Context getContext() {
    return context;
  }

  public void setPackageFilter(String packageFilter) {
    this.packageFilter = packageFilter;
  }

  public String getPackageFilter() {
    return packageFilter;
  }


  public TreeLogger getTreeLogger() {
    return treeLogger;
  }

  public GeneratorContext getGeneratorContext() {
    return generatorContext;
  }

  public SourceWriter getWriter() {
    return writer;
  }

  public TypeOracle getOracle() {
    return oracle;
  }

  public QualifyingMetadataFactory getQualifyingMetadataFactory() {
    return qualifyingMetadataFactory;
  }

  public void setQualifyingMetadataFactory(QualifyingMetadataFactory qualifyingMetadataFactory) {
    this.qualifyingMetadataFactory = qualifyingMetadataFactory;
  }
}
