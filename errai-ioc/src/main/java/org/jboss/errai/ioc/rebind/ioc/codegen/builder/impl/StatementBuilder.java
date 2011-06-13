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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanExpression;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ArrayInitializationBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBegin;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.VariableReferenceContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeclareVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DynamicLoad;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadField;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadLiteral;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.MethodCall;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * The root of our fluent StatementBuilder API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class StatementBuilder extends AbstractStatementBuilder implements StatementBegin {

  public StatementBuilder(Context context) {
    super(context);

    if (context != null) {
      for (Variable v : context.getDeclaredVariables()) {
        appendCallElement(new DeclareVariable(v));
      }
    }
  }

  public static StatementBegin create() {
    return new StatementBuilder(null);
  }

  public static StatementBegin create(Context context) {
    return new StatementBuilder(context);
  }

  public StatementBuilder addVariable(String name, Class<?> type) {
    Variable v = Variable.create(name, type);
    return addVariable(v);
  }

  public StatementBuilder addVariable(String name, TypeLiteral<?> type) {
    Variable v = Variable.create(name, type);
    return addVariable(v);
  }

  public StatementBuilder addVariable(String name, Object initialization) {
    Variable v = Variable.create(name, initialization);
    return addVariable(v);
  }

  public StatementBuilder addVariable(String name, Class<?> type, Object initialization) {
    Variable v = Variable.create(name, type, initialization);
    return addVariable(v);
  }

  public StatementBuilder addVariable(String name, TypeLiteral<?> type, Object initialization) {
    Variable v = Variable.create(name, type, initialization);
    return addVariable(v);
  }

  private StatementBuilder addVariable(Variable v) {
    appendCallElement(new DeclareVariable(v));
    return this;
  }

  public VariableReferenceContextualStatementBuilder loadVariable(String name, Object... indexes) {
    appendCallElement(new LoadVariable(name, indexes));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  public ContextualStatementBuilder loadLiteral(Object o) {
    appendCallElement(new LoadLiteral(o));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  public ContextualStatementBuilder load(Object o) {
    appendCallElement(new DynamicLoad(o));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  public ContextualStatementBuilder invokeStatic(Class<?> clazz, String methodName, Object... parameters) {
    appendCallElement(new LoadClassReference(clazz));
    appendCallElement(new MethodCall(methodName, parameters, true));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  public ContextualStatementBuilder loadStatic(Class<?> clazz, String fieldName) {
    appendCallElement(new LoadClassReference(clazz));
    appendCallElement(new LoadField(fieldName));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  public ObjectBuilder newObject(Class<?> type) {
    return ObjectBuilder.newInstanceOf(type);
  }

  public ObjectBuilder newObject(TypeLiteral<?> type) {
    return ObjectBuilder.newInstanceOf(type);
  }
  
  public ArrayInitializationBuilder newArray(Class<?> componentType) {
    return new ArrayBuilderImpl(context, callElementBuilder).newArray(componentType);
  }

  public ArrayInitializationBuilder newArray(Class<?> componentType, Integer... dimensions) {
    return new ArrayBuilderImpl(context, callElementBuilder).newArray(componentType, dimensions);
  }

  public BlockBuilder<ElseBlockBuilder> doIf(BooleanExpression stmt) {
    return new IfBlockBuilderImpl(context, callElementBuilder).if_(stmt);
  }

  public BlockBuilder<LoopBuilder> whileLoop(BooleanExpression stmt) {
    return new LoopBuilderImpl(context, callElementBuilder).while_(stmt);
  }

  public BlockBuilder<LoopBuilder> forLoop(BooleanExpression condition) {
    return new LoopBuilderImpl(context, callElementBuilder).for_(condition);
  }

  public BlockBuilder<LoopBuilder> forLoop(Statement initializer, BooleanExpression condition) {
    return new LoopBuilderImpl(context, callElementBuilder).for_(initializer, condition);
  }

  public BlockBuilder<LoopBuilder> forLoop(Statement initializer, BooleanExpression condition, Statement countingExpression) {
    return new LoopBuilderImpl(context, callElementBuilder).for_(initializer, condition, countingExpression);
  }
}