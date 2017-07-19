/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.codegen.util;

import org.jboss.errai.codegen.StringExpression;
import org.jboss.errai.codegen.StringOperator;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.impl.StringExpressionBuilder;

/**
 * @author Slava Pankov
 */
public class Str {

  public static StringExpression expr(final Object lhs, final StringOperator operator, final Object rhs) {
    return StringExpressionBuilder.create(lhs, operator, rhs);
  }

  public static StringExpression expr(final StringOperator operator, final Object rhs) {
    return StringExpressionBuilder.create(operator, rhs);
  }

  public static StringExpression expr(final Statement lhs) {
    return StringExpressionBuilder.create(lhs);
  }
}
