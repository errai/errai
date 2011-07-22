/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCProcessingContext extends ProcessingContext {
  protected Context context;
  protected MetaClass bootstrapClass;
  protected BlockBuilder<?> blockBuilder;
  protected String packageFilter;

  public IOCProcessingContext(TreeLogger treeLogger,
                              GeneratorContext generatorContext,
                              SourceWriter writer,
                              TypeOracle oracle,
                              Context context,
                              MetaClass bootstrapClass,
                              BlockBuilder<?> blockBuilder) {
    super(treeLogger, generatorContext, writer, oracle);
    this.context = context;
    this.bootstrapClass = bootstrapClass;
    this.blockBuilder = blockBuilder;
  }

  public IOCProcessingContext(ProcessingContext ctx,
                              Context context,
                              MetaClass bootstrapClass,
                              BlockBuilder<?> blockBuilder) {
    super(ctx.getTreeLogger(), ctx.getGeneratorContext(), ctx.getWriter(), ctx.getOracle());
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


}
