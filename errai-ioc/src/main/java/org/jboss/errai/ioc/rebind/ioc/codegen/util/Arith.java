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

package org.jboss.errai.ioc.rebind.ioc.codegen.util;

import org.jboss.errai.ioc.rebind.ioc.codegen.ArithmeticExpression;
import org.jboss.errai.ioc.rebind.ioc.codegen.ArithmeticOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ArithmeticExpressionBuilder;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Arith {

  public static ArithmeticExpression expr(Object lhs, ArithmeticOperator operator, Object rhs) {
    return ArithmeticExpressionBuilder.create(lhs, operator, rhs);
  }

  public static ArithmeticExpression expr(ArithmeticOperator operator, Object rhs) {
    return ArithmeticExpressionBuilder.create(operator, rhs);
  }

  public static ArithmeticExpression expr(Statement lhs) {
    return ArithmeticExpressionBuilder.create(lhs);
  }
}