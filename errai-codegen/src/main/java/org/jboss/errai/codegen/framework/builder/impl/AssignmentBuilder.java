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

import org.jboss.errai.codegen.framework.AssignmentOperator;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.VariableReference;
import org.jboss.errai.codegen.framework.exception.InvalidTypeException;
import org.jboss.errai.codegen.framework.meta.MetaClass;

/**
 * {@link StatementBuilder} that generates {@link VariableReference} assignments.
 * 
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class AssignmentBuilder implements Statement {
  protected boolean isFinal;

  protected AssignmentOperator operator;
  protected VariableReference reference;
  protected Statement statement;

  public AssignmentBuilder(boolean isFinal, AssignmentOperator operator, VariableReference reference,
                           Statement statement) {
    this.isFinal = isFinal;
    this.operator = operator;
    this.reference = reference;
    this.statement = statement;
  }

  @Override
  public String generate(Context context) {
    MetaClass referenceType = reference.getType();
    Statement[] indexes = reference.getIndexes();
    if (indexes!=null) {
      for (Statement index : indexes) {
        if (!referenceType.isArray())
          throw new InvalidTypeException("Variable is not a " + indexes.length + "-dimensional array!");
        referenceType = referenceType.getComponentType();
      } 
    }
    operator.assertCanBeApplied(referenceType);
    operator.assertCanBeApplied(statement.getType());

    return  reference.generate(context) + generateIndexes(context, indexes) +
        " " + operator.getCanonicalString() + " " + statement.generate(context);
  }

  private String generateIndexes(Context context, Statement[] indexes) {
    if (indexes==null || indexes.length == 0) return "";
   
    StringBuilder buf = new StringBuilder();
    for (Statement index : indexes) {
      buf.append("[").append(index.generate(context)).append("]");
    }
    return buf.toString();
  }

  @Override
  public MetaClass getType() {
    return reference.getType();
  }
}