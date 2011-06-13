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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanExpression;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface StatementBegin extends ArrayBuilder {

  public StatementBuilder addVariable(String name, Class<?> type);

  public StatementBuilder addVariable(String name, TypeLiteral<?> type);

  public StatementBuilder addVariable(String name, Object initialization);

  public StatementBuilder addVariable(String name, Class<?> type, Object initialization);

  public StatementBuilder addVariable(String name, TypeLiteral<?> type, Object initialization);

  public VariableReferenceContextualStatementBuilder loadVariable(String name, Object... indexes);

  public ContextualStatementBuilder loadLiteral(Object o);

  public ContextualStatementBuilder load(Object o);

  public ContextualStatementBuilder invokeStatic(Class<?> clazz, String methodName, Object... parameters);

  public ContextualStatementBuilder loadStatic(Class<?> clazz, String fieldName);

  public ObjectBuilder newObject(MetaClass type);

  public ObjectBuilder newObject(Class<?> type);

  public BlockBuilder<ElseBlockBuilder> doIf(BooleanExpression condition);
  
  public BlockBuilder<LoopBuilder> whileLoop(BooleanExpression condition);
  
  public BlockBuilder<LoopBuilder> forLoop(BooleanExpression condition);
  
  public BlockBuilder<LoopBuilder> forLoop(Statement initializer, BooleanExpression condition);
  
  public BlockBuilder<LoopBuilder> forLoop(Statement initializer, BooleanExpression condition, Statement countingExpression);
}