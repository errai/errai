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

import org.jboss.errai.codegen.BooleanExpression;
import org.jboss.errai.codegen.BooleanOperator;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.UnaryOperator;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BooleanExpressionBuilder extends ExpressionBuilder<BooleanOperator> implements BooleanExpression {
  private boolean negated;

  public BooleanExpressionBuilder() {}

  public BooleanExpressionBuilder(Statement rhs, BooleanOperator operator) {
    super(rhs, operator);
  }

  public BooleanExpressionBuilder(Statement lhs, Statement rhs, BooleanOperator operator) {
    super(lhs, rhs, operator);
  }

  public BooleanExpressionBuilder(Object lhs, Object rhs, BooleanOperator operator) {
    super(lhs, rhs, operator);
  }
  
  public static BooleanExpression create(Statement lhs) {
    return new BooleanExpressionBuilder(lhs, null, null);
  }

  public static BooleanExpression create(BooleanOperator operator, Object rhs) {
    return create(null, operator, rhs);
  }

  public static BooleanExpression create(Object lhs, BooleanOperator operator, Object rhs) {
    return new BooleanExpressionBuilder(lhs, rhs, operator);
  }

  @Override
  public String generate(Context context) {
    if (operator == null) {
      lhs = GenUtil.generate(context, lhs);
      lhs = GenUtil.convert(context, lhs, MetaClassFactory.get(Boolean.class));
    }

    String expr = super.generate(context);

    if (negated) {
      return UnaryOperator.Complement.getCanonicalString()  + "(" + expr + ")";
    }
    
    return expr;
  }

  @Override
  public MetaClass getType() {
    return MetaClassFactory.get(boolean.class);
  }

  @Override
  public BooleanExpression negate() {
    this.negated = !negated;
    return this;
  }
}