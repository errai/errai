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

package org.jboss.errai.codegen.framework.builder.impl;

import javax.enterprise.util.TypeLiteral;

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.codegen.framework.BooleanExpression;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.ArrayInitializationBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.CaseBlockBuilder;
import org.jboss.errai.codegen.framework.builder.CatchBlockBuilder;
import org.jboss.errai.codegen.framework.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.framework.builder.ElseBlockBuilder;
import org.jboss.errai.codegen.framework.builder.StatementBegin;
import org.jboss.errai.codegen.framework.builder.StatementEnd;
import org.jboss.errai.codegen.framework.builder.VariableDeclarationInitializer;
import org.jboss.errai.codegen.framework.builder.VariableDeclarationNamed;
import org.jboss.errai.codegen.framework.builder.VariableDeclarationStart;
import org.jboss.errai.codegen.framework.builder.VariableReferenceContextualStatementBuilder;
import org.jboss.errai.codegen.framework.builder.WhileBuilder;
import org.jboss.errai.codegen.framework.builder.callstack.BranchCallElement;
import org.jboss.errai.codegen.framework.builder.callstack.DeclareVariable;
import org.jboss.errai.codegen.framework.builder.callstack.DefineLabel;
import org.jboss.errai.codegen.framework.builder.callstack.DynamicLoad;
import org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.framework.builder.callstack.LoadField;
import org.jboss.errai.codegen.framework.builder.callstack.LoadLiteral;
import org.jboss.errai.codegen.framework.builder.callstack.LoadNested;
import org.jboss.errai.codegen.framework.builder.callstack.LoadVariable;
import org.jboss.errai.codegen.framework.builder.callstack.MethodCall;
import org.jboss.errai.codegen.framework.builder.callstack.ResetCallElement;
import org.jboss.errai.codegen.framework.builder.callstack.ThrowException;
import org.jboss.errai.codegen.framework.control.branch.BreakStatement;
import org.jboss.errai.codegen.framework.control.branch.ContinueStatement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

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
        if (v.getName().matches("(this|super)")) continue;
        appendCallElement(new DeclareVariable(v));
      }
      appendCallElement(new ResetCallElement());
    }
  }

  public static StatementBegin create() {
    return new StatementBuilder(null);
  }

  public static StatementBegin create(Context context) {
    return new StatementBuilder(context);
  }

  @Override
  public VariableDeclarationStart declareVariable(Class<?> type) {
    return declareVariable(MetaClassFactory.get(type));
  }

  @Override
  public VariableDeclarationStart declareVariable(final MetaClass type) {
    return new VariableDeclarationStart<StatementBuilder>() {
      boolean isFinal;
      String name;
      Object initialization;

      @Override
      public VariableDeclarationNamed<StatementBuilder> asFinal() {
        isFinal = true;
        return this;
      }

      @Override
      public VariableDeclarationInitializer<StatementBuilder> named(String name) {
        this.name = name;
        return this;
      }

      @Override
      public StatementBuilder initializeWith(Object initialization) {
        this.initialization = initialization;
        return finish();
      }

      @Override
      public StatementBuilder initializeWith(Statement initialization) {
        this.initialization = initialization;
        return finish();
      }

      @Override
      public StatementBuilder finish() {
        if (initialization == null) {
          declareVariable(isFinal ? Variable.createFinal(name, type) : Variable.create(name, type));
        }
        else {
          declareVariable(isFinal ? Variable.createFinal(name, type, initialization) : Variable.create(name, type,
                  initialization));
        }
        return StatementBuilder.this;
      }
    };
  }

  @Override
  public StatementBuilder declareVariable(String name, Class<?> type) {
    Variable v = Variable.create(name, type);
    return declareVariable(v);
  }

  @Override
  public StatementBuilder declareVariable(String name, TypeLiteral<?> type) {
    Variable v = Variable.create(name, type);
    return declareVariable(v);
  }

  @Override
  public StatementBuilder declareVariable(String name, Object initialization) {
    Variable v = Variable.create(name, initialization);
    return declareVariable(v);
  }

  @Override
  public StatementBuilder declareVariable(String name, Class<?> type, Object initialization) {
    Variable v = Variable.create(name, type, initialization);
    return declareVariable(v);
  }

  @Override
  public StatementBuilder declareVariable(String name, TypeLiteral<?> type, Object initialization) {
    Variable v = Variable.create(name, type, initialization);
    return declareVariable(v);
  }

  private StatementBuilder declareVariable(Variable v) {
    appendCallElement(new DeclareVariable(v));
    return this;
  }

  @Override
  public VariableReferenceContextualStatementBuilder loadVariable(String name, Object... indexes) {
    if (name.matches("(this.)(.)*"))
      return loadClassMember(name.replaceFirst("(this.)", ""), indexes);

    appendCallElement(new LoadVariable(name, indexes));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  @Override
  public VariableReferenceContextualStatementBuilder loadClassMember(String name, Object... indexes) {
    appendCallElement(new LoadVariable(name, true, indexes));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  @Override
  public ContextualStatementBuilder loadLiteral(Object o) {
    appendCallElement(new LoadLiteral(o));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  @Override
  public ContextualStatementBuilder load(Object o) {
    appendCallElement(new DynamicLoad(o));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  @Override
  public ContextualStatementBuilder loadClassReference(Object o) {
    MetaClass c;
    if (o instanceof MetaClass) {
      c = (MetaClass) o;
    }
    else if (o instanceof Class) {
      c = MetaClassFactory.get((Class) o);
    }
    else if (o instanceof JClassType) {
      c = MetaClassFactory.get((JClassType) o);
    }
    else {
      throw new RuntimeException("unknown class reference type: " + (o == null ? "null" : o.getClass().getName()));
    }
    appendCallElement(new LoadClassReference(c));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  @Override
  public ContextualStatementBuilder invokeStatic(MetaClass clazz, String methodName, Object... parameters) {
    appendCallElement(new LoadClassReference(clazz));
    appendCallElement(new MethodCall(methodName, parameters, true));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  @Override
  public ContextualStatementBuilder invokeStatic(Class<?> clazz, String methodName, Object... parameters) {
    return invokeStatic(MetaClassFactory.get(clazz), methodName, parameters);
  }

  @Override
  public ContextualStatementBuilder loadStatic(Class<?> clazz, String fieldName) {
    return loadStatic(MetaClassFactory.get(clazz), fieldName);
  }

  @Override
  public ContextualStatementBuilder loadStatic(MetaClass clazz, String fieldName) {
    appendCallElement(new LoadClassReference(clazz));
    appendCallElement(new LoadField(fieldName));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  @Override
  public ContextualStatementBuilder nestedCall(Statement statement) {
    appendCallElement(new LoadNested(statement));
    return new ContextualStatementBuilderImpl(context, callElementBuilder);
  }

  @Override
  public ObjectBuilder newObject(Class<?> type) {
    return ObjectBuilder.newInstanceOf(type, context, callElementBuilder);
  }

  @Override
  public ObjectBuilder newObject(MetaClass type) {
    return ObjectBuilder.newInstanceOf(type, context, callElementBuilder);
  }

  @Override
  public ObjectBuilder newObject(TypeLiteral<?> type) {
    return ObjectBuilder.newInstanceOf(type, context, callElementBuilder);
  }

//  @Override
//  public ArrayInitializationBuilder newArray(Class<?> componentType) {
//    return new ArrayBuilderImpl(context, callElementBuilder).newArray(componentType);
//  }

  @Override
  public ArrayInitializationBuilder newArray(Class<?> componentType, Object... dimensions) {
    return new ArrayBuilderImpl(context, callElementBuilder).newArray(componentType, dimensions);
  }

  @Override
  public BlockBuilder<WhileBuilder> do_() {
    return new LoopBuilderImpl(context, callElementBuilder).do_();
  }

  @Override
  public BlockBuilder<ElseBlockBuilder> if_(BooleanExpression stmt) {
    return new IfBlockBuilderImpl(context, callElementBuilder).if_(stmt);
  }

  @Override
  public BlockBuilder<StatementEnd> while_(BooleanExpression stmt) {
    return new LoopBuilderImpl(context, callElementBuilder).while_(stmt);
  }

  @Override
  public BlockBuilder<StatementEnd> for_(BooleanExpression condition) {
    return new LoopBuilderImpl(context, callElementBuilder).for_(condition);
  }

  @Override
  public BlockBuilder<StatementEnd> for_(Statement initializer, BooleanExpression condition) {
    return new LoopBuilderImpl(context, callElementBuilder).for_(initializer, condition);
  }

  @Override
  public BlockBuilder<StatementEnd> for_(Statement initializer, BooleanExpression condition,
                                         Statement countingExpression) {
    return new LoopBuilderImpl(context, callElementBuilder).for_(initializer, condition, countingExpression);
  }

  @Override
  public CaseBlockBuilder switch_(Statement statement) {
    return new SwitchBlockBuilderImpl(context, callElementBuilder).switch_(statement);
  }

  @Override
  public BlockBuilder<CatchBlockBuilder> try_() {
    return new TryBlockBuilderImpl(context, callElementBuilder).try_();
  }

  @Override
  public StatementEnd throw_(Class<? extends Throwable> throwableType, Object... parameters) {
    appendCallElement(new ThrowException(throwableType, parameters));
    return this;
  }

  @Override
  public StatementEnd throw_(String exceptionVarName) {
    appendCallElement(new ThrowException(exceptionVarName));
    return this;
  }

  @Override
  public StatementEnd label(String label) {
    appendCallElement(new DefineLabel(label));
    return this;
  }

  @Override
  public StatementEnd break_() {
    appendCallElement(new BranchCallElement(new BreakStatement()));
    return this;
  }

  @Override
  public StatementEnd break_(String label) {
    appendCallElement(new BranchCallElement(new BreakStatement(label)));
    return this;
  }

  @Override
  public StatementEnd continue_() {
    appendCallElement(new BranchCallElement(new ContinueStatement()));
    return this;
  }

  @Override
  public StatementEnd continue_(String label) {
    appendCallElement(new BranchCallElement(new ContinueStatement(label)));
    return this;
  }
}