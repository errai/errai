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

package org.jboss.errai.codegen.framework.util;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.framework.BooleanExpression;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.ArrayInitializationBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.CaseBlockBuilder;
import org.jboss.errai.codegen.framework.builder.CatchBlockBuilder;
import org.jboss.errai.codegen.framework.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.framework.builder.ElseBlockBuilder;
import org.jboss.errai.codegen.framework.builder.StatementBegin;
import org.jboss.errai.codegen.framework.builder.StatementEnd;
import org.jboss.errai.codegen.framework.builder.VariableDeclarationStart;
import org.jboss.errai.codegen.framework.builder.VariableReferenceContextualStatementBuilder;
import org.jboss.errai.codegen.framework.builder.WhileBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.builder.impl.StatementBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Stmt {
  public static StatementBegin create() {
    return StatementBuilder.create();
  }
  
  public static StatementBegin create(Context ctx) {
    return StatementBuilder.create(ctx);
  }

  public static ArrayInitializationBuilder newArray(Class<?> componentType) {
    return StatementBuilder.create().newArray(componentType);
  }

  public static ArrayInitializationBuilder newArray(Class<?> componentType, Object... dimensions) {
    return StatementBuilder.create().newArray(componentType, dimensions);
  }

  public static BlockBuilder<WhileBuilder> do_() {
    return StatementBuilder.create().do_();
  }

  public static BlockBuilder<StatementEnd> while_(BooleanExpression condition) {
    return StatementBuilder.create().while_(condition);
  }

  public static BlockBuilder<StatementEnd> for_(BooleanExpression condition) {
    return StatementBuilder.create().for_(condition);
  }

  public static BlockBuilder<StatementEnd> for_(Statement initializer, BooleanExpression condition) {
    return StatementBuilder.create().for_(initializer, condition);
  }

  public static BlockBuilder<StatementEnd> for_(Statement initializer, BooleanExpression condition,
      Statement countingExpression) {
    return StatementBuilder.create().for_(initializer, condition, countingExpression);
  }

  public static BlockBuilder<ElseBlockBuilder> if_(BooleanExpression condition) {
    return StatementBuilder.create().if_(condition);
  }

  public static CaseBlockBuilder switch_(Statement statement) {
    return StatementBuilder.create().switch_(statement);
  }

  public static BlockBuilder<CatchBlockBuilder> try_() {
    return StatementBuilder.create().try_();
  }

  public static VariableDeclarationStart<StatementBegin> declareVariable(Class<?> type) {
    return StatementBuilder.create().declareVariable(type);
  }

  public static VariableDeclarationStart<StatementBegin> declareVariable(MetaClass type) {
    return StatementBuilder.create().declareVariable(type);
  }

  public static StatementBuilder declareVariable(String name, Class<?> type) {
    return StatementBuilder.create().declareVariable(name, type);
  }

  public static StatementBuilder declareVariable(String name, TypeLiteral<?> type) {
    return StatementBuilder.create().declareVariable(name, type);
  }

  public static StatementBuilder declareVariable(String name, Object initialization) {
    return StatementBuilder.create().declareVariable(name, initialization);
  }

  public static StatementBuilder declareVariable(String name, Class<?> type, Object initialization) {
    return StatementBuilder.create().declareVariable(name, type, initialization);
  }

  public static StatementBuilder declareVariable(String name, TypeLiteral<?> type, Object initialization) {
    return StatementBuilder.create().declareVariable(name, type, initialization);
  }

  public static VariableReferenceContextualStatementBuilder loadVariable(String name, Object... indexes) {
    return StatementBuilder.create().loadVariable(name, indexes);
  }

  public static VariableReferenceContextualStatementBuilder loadClassMember(String name, Object... indexes) {
    return StatementBuilder.create().loadClassMember(name, indexes);
  }

  public static ContextualStatementBuilder loadLiteral(Object o) {
    return StatementBuilder.create().loadLiteral(o);
  }

  public static ContextualStatementBuilder load(Object o) {
    return StatementBuilder.create().load(o);
  }

  public static ContextualStatementBuilder loadClassReference(Object o) {
    return StatementBuilder.create().loadClassReference(o);
  }

  public static ContextualStatementBuilder invokeStatic(MetaClass clazz, String methodName, Object... parameters) {
    return StatementBuilder.create().invokeStatic(clazz, methodName, parameters);
  }

  public static ContextualStatementBuilder invokeStatic(Class<?> clazz, String methodName, Object... parameters) {
    return StatementBuilder.create().invokeStatic(clazz, methodName, parameters);
  }

  public static ContextualStatementBuilder loadStatic(Class<?> clazz, String fieldName) {
    return StatementBuilder.create().loadStatic(clazz, fieldName);
  }

  public static ContextualStatementBuilder loadStatic(MetaClass clazz, String fieldName) {
    return StatementBuilder.create().loadStatic(clazz, fieldName);
  }

  public static ContextualStatementBuilder nestedCall(Statement statement) {
    return StatementBuilder.create().nestedCall(statement);
  }

  public static ObjectBuilder newObject(Class<?> type) {
    return StatementBuilder.create().newObject(type);
  }

  public static ObjectBuilder newObject(MetaClass type) {
    return StatementBuilder.create().newObject(type);
  }

  public static ObjectBuilder newObject(TypeLiteral<?> type) {
    return StatementBuilder.create().newObject(type);
  }

  public static StatementEnd throw_(Class<? extends Throwable> throwableType, Object... parameters) {
    return StatementBuilder.create().throw_(throwableType, parameters);
  }

  public static StatementEnd throw_(String exceptionVarName) {
    return StatementBuilder.create().throw_(exceptionVarName);
  }

  public static StatementEnd label(String label) {
    return StatementBuilder.create().label(label);
  }

  public static StatementEnd break_() {
    return StatementBuilder.create().break_();
  }

  public static StatementEnd break_(String label) {
    return StatementBuilder.create().break_(label);
  }

  public static StatementEnd continue_() {
    return StatementBuilder.create().continue_();
  }

  public static StatementEnd continue_(String label) {
    return StatementBuilder.create().continue_(label);
  }
}
