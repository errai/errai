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

package org.jboss.errai.codegen.builder.impl;

import org.jboss.errai.codegen.ArithmeticExpression;
import org.jboss.errai.codegen.ArithmeticOperator;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ArithmeticExpressionBuilder extends ExpressionBuilder<ArithmeticOperator> implements ArithmeticExpression {

  public ArithmeticExpressionBuilder() {}

  public ArithmeticExpressionBuilder(Statement rhs, ArithmeticOperator operator) {
    super(rhs, operator);
  }

  public ArithmeticExpressionBuilder(Statement lhs, Statement rhs, ArithmeticOperator operator) {
    super(lhs, rhs, operator);
  }

  public ArithmeticExpressionBuilder(Object lhs, Object rhs, ArithmeticOperator operator) {
    super(lhs, rhs, operator);
  }
  
  public static ArithmeticExpression create(Statement lhs) {
    return new ArithmeticExpressionBuilder(lhs, null, null);
  }

  public static ArithmeticExpression create(ArithmeticOperator operator, Object rhs) {
    return create(null, operator, rhs);
  }

  public static ArithmeticExpression create(Object lhs, ArithmeticOperator operator, Object rhs) {
    return new ArithmeticExpressionBuilder(lhs, rhs, operator);
  }

  @Override
  public MetaClass getType() {
    return MetaClassFactory.get(Number.class);
  }
}