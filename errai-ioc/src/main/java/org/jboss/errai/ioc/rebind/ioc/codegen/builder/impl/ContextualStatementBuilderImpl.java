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

import org.jboss.errai.ioc.rebind.ioc.codegen.AssignmentOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanExpression;
import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementEnd;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.VariableReferenceContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.WhileBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.AssignVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadField;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.MethodCall;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.ReturnValue;

/**
 * Implementation of the {@link ContextualStatementBuilder}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContextualStatementBuilderImpl extends AbstractStatementBuilder implements ContextualStatementBuilder,
    VariableReferenceContextualStatementBuilder {

  protected ContextualStatementBuilderImpl(Context context, CallElementBuilder callElementBuilder) {
    super(context, callElementBuilder);
  }

  // Invocation
  public ContextualStatementBuilder invoke(String methodName, Object... parameters) {
    appendCallElement(new MethodCall(methodName, parameters));
    return this;
  }

  public ContextualStatementBuilder getField(String fieldName) {
    appendCallElement(new LoadField(fieldName));
    return this;
  }

  // Looping
  public BlockBuilder<StatementEnd> foreach(String loopVarName) {
    return new LoopBuilderImpl(context, callElementBuilder).foreach(loopVarName);
  }

  public BlockBuilder<StatementEnd> foreach(String loopVarName, Class<?> loopVarType) {
    return new LoopBuilderImpl(context, callElementBuilder).foreach(loopVarName, loopVarType);
  }

  public BlockBuilder<StatementEnd> for_(BooleanExpression condition) {
    return new LoopBuilderImpl(context, callElementBuilder).for_(condition);
  }

  public BlockBuilder<StatementEnd> for_(BooleanExpression condition, Statement afterBlock) {
    return new LoopBuilderImpl(context, callElementBuilder).for_(condition, afterBlock);
  }

  public BlockBuilder<WhileBuilder> do_() {
    return new LoopBuilderImpl(context, callElementBuilder).do_();
  }

  public BlockBuilder<StatementEnd> while_() {
    return new LoopBuilderImpl(context, callElementBuilder).while_();
  }

  public BlockBuilder<StatementEnd> while_(BooleanOperator op, Statement rhs) {
    return new LoopBuilderImpl(context, callElementBuilder).while_(op, rhs);
  }

  public BlockBuilder<StatementEnd> while_(BooleanOperator op, Object rhs) {
    return new LoopBuilderImpl(context, callElementBuilder).while_(op, rhs);
  }

  // If-Then-Else
  public BlockBuilder<ElseBlockBuilder> if_() {
    return new IfBlockBuilderImpl(context, callElementBuilder).if_();
  }

  public BlockBuilder<ElseBlockBuilder> if_(BooleanOperator op, Statement rhs) {
    return new IfBlockBuilderImpl(context, callElementBuilder).if_(op, rhs);
  }

  public BlockBuilder<ElseBlockBuilder> if_(BooleanOperator op, Object rhs) {
    return new IfBlockBuilderImpl(context, callElementBuilder).if_(op, rhs);
  }

  // Value return
  public Statement returnValue() {
    appendCallElement(new ReturnValue());
    return this;
  }

  // Assignments
  public StatementEnd assignValue(Object statement) {
    return assignValue(AssignmentOperator.Assignment, statement);
  }

  public StatementEnd assignValue(AssignmentOperator operator, Object statement) {
    appendCallElement(new AssignVariable(operator, statement));
    return this;
  }
}