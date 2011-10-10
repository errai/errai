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

import org.jboss.errai.codegen.framework.BooleanExpression;
import org.jboss.errai.codegen.framework.BooleanOperator;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.impl.BooleanExpressionBuilder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Bool {

  public static BooleanExpression expr(Object lhs, BooleanOperator operator, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, operator, rhs);
  }

  public static BooleanExpression expr(BooleanOperator operator, Object rhs) {
    return BooleanExpressionBuilder.create(operator, rhs);
  }

  public static BooleanExpression expr(Statement lhs) {
    return BooleanExpressionBuilder.create(lhs);
  }
  
  public static BooleanExpression equals(Object lhs, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, BooleanOperator.Equals, rhs);
  }
  
  public static BooleanExpression notEquals(Object lhs, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, BooleanOperator.NotEquals, rhs);
  }
  
  public static BooleanExpression or(Object lhs, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, BooleanOperator.Or, rhs);
  }
  
  public static BooleanExpression and(Object lhs, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, BooleanOperator.And, rhs);
  }
  
  public static BooleanExpression instanceOf(Object lhs, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, BooleanOperator.InstanceOf, rhs);
  }
  
  public static BooleanExpression greaterThanOrEqual(Object lhs, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, BooleanOperator.GreaterThanOrEqual, rhs);
  }
  
  public static BooleanExpression greaterThan(Object lhs, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, BooleanOperator.GreaterThan, rhs);
  }
  
  public static BooleanExpression lessThanOrEqual(Object lhs, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, BooleanOperator.LessThanOrEqual, rhs);
  }
  
  public static BooleanExpression lessThan(Object lhs, Object rhs) {
    return BooleanExpressionBuilder.create(lhs, BooleanOperator.LessThan, rhs);
  }
  
  public static BooleanExpression isNotNull(Object test) {
    return Bool.notEquals(test, null);
  }
  
  public static BooleanExpression isNull(Object test) {
    return Bool.equals(test, null);
  }
}
