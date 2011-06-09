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

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BooleanExpressionBuilder implements Statement {
  private Statement lhs;
  private String lhsExpr;
  private Statement rhs;
  private BooleanOperator operator;

  public BooleanExpressionBuilder() {}

  public BooleanExpressionBuilder(Statement rhs, BooleanOperator operator) {
    this.rhs = rhs;
    this.operator = operator;
  }

  public BooleanExpressionBuilder(Statement lhs, Statement rhs, BooleanOperator operator) {
    this(rhs, operator);
    this.lhs = lhs;
  }

  public BooleanExpressionBuilder(String lhsExpr, Statement rhs, BooleanOperator operator) {
    this(rhs, operator);
    this.lhsExpr = lhsExpr;
  }

  public static Statement create(Object lhs, BooleanOperator operator, Object rhs) {
    Statement toLhs;
    Statement toRhs;

    if (lhs instanceof Statement) {
      toLhs = (Statement) lhs;
    }
    else {
      toLhs = LiteralFactory.getLiteral(lhs);
    }

    if (rhs instanceof Statement) {
      toRhs = (Statement) rhs;
    }
    else {
      toRhs = LiteralFactory.getLiteral(rhs);
    }

    return new BooleanExpressionBuilder(toLhs, toRhs, operator);
  }

  public BooleanOperator getOperator() {
    return operator;
  }

  public void setLhs(Statement lhs) {
    this.lhs = lhs;
  }

  public void setLhsExpr(String lhsExpr) {
    this.lhsExpr = lhsExpr;
  }

  public String generate(Context context) {
    if (operator != null) {
      if (lhs != null)
        operator.assertCanBeApplied(GenUtil.generate(context, lhs).getType());
      if (rhs != null)
        operator.assertCanBeApplied(GenUtil.generate(context, rhs).getType());
    }
    else {
      lhs = GenUtil.convert(context, lhs, MetaClassFactory.get(Boolean.class));
    }

    return "(" + ((lhsExpr == null) ? lhs.generate(context) : lhsExpr)
        + ((operator != null) ? (" " + operator.getCanonicalString()) : "")
        + ((rhs != null) ? (" " + rhs.generate(context)) : "") + ")";
  }

  public Context getContext() {
    return null;
  }

  public MetaClass getType() {
    return MetaClassFactory.get(boolean.class);
  }
}
