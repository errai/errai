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

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanExpression;
import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BooleanExpressionBuilder implements BooleanExpression {
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

  public static BooleanExpression create(Statement lhs) {
    return new BooleanExpressionBuilder(lhs, null, null);
  }
  
  public static BooleanExpression create(BooleanOperator operator, Object rhs) {
    return create(null, operator, rhs);
  }
  
  public static BooleanExpression create(Object lhs, BooleanOperator operator, Object rhs) {
    Statement toLhs = null;
    Statement toRhs = null;

    if (lhs != null) {
      if (lhs instanceof Statement) {
        toLhs = (Statement) lhs;
      }
      else {
        toLhs = LiteralFactory.getLiteral(lhs);
      }
    }

    if (rhs instanceof Statement) {
      toRhs = (Statement) rhs;
    }
    else {
      toRhs = LiteralFactory.getLiteral(rhs);
    }

    return new BooleanExpressionBuilder(toLhs, toRhs, operator);
  }

  @Override
  public String generate(Context context) {
    if (operator != null) {
      if (lhs != null)
        operator.assertCanBeApplied(GenUtil.generate(context, lhs).getType());
      if (rhs != null)
        operator.assertCanBeApplied(GenUtil.generate(context, rhs).getType());
    }
    else {
      lhs = GenUtil.generate(context, lhs);
      lhs = GenUtil.convert(context, lhs, MetaClassFactory.get(Boolean.class));
    }

    String lhsExpr = "";
    String operExpr = "";
    String rhsExpr = "";

    if (this.lhsExpr != null) {
      lhsExpr = this.lhsExpr;
    }
    else if (lhs != null) {
      if (lhs instanceof BooleanExpressionBuilder && this.operator != null) {
        lhsExpr = "(" + lhs.generate(context) + ")";
      }
      else {
        lhsExpr = lhs.generate(context);
      }
    }

    if (this.operator != null) {
      operExpr = " " + this.operator.getCanonicalString() + " ";
    }

    if (rhs != null) {
      if (rhs instanceof BooleanExpressionBuilder) {
        rhsExpr = "(" + rhs.generate(context) + ")";
      }
      else {
        rhsExpr = rhs.generate(context);
      }
    }

    return lhsExpr + operExpr + rhsExpr;
  }

  @Override
  public MetaClass getType() {
    return MetaClassFactory.get(boolean.class);
  }

  @Override
  public Statement getLhs() {
    return lhs;
  }

  @Override
  public void setLhs(Statement lhs) {
    this.lhs = lhs;
  }
  
  @Override
  public String getLhsExpr() {
    return lhsExpr;
  }

  @Override
  public void setLhsExpr(String lhsExpr) {
    this.lhsExpr = lhsExpr;
  }
  
  @Override
  public Statement getRhs() {
    return rhs;
  }

  @Override
  public void setRhs(Statement rhs) {
    this.rhs = rhs;
  }

  @Override
  public BooleanOperator getOperator() {
    return operator;
  }
  
  @Override
  public void setOperator(BooleanOperator operator) {
    this.operator = operator;
  }
}
