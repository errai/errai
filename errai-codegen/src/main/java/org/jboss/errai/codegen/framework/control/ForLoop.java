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

package org.jboss.errai.codegen.framework.control;

import org.jboss.errai.codegen.framework.BlockStatement;
import org.jboss.errai.codegen.framework.BooleanExpression;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ForLoop extends AbstractConditionalBlock {
  private Statement initializer;
  private Statement afterBlock;

  public ForLoop(BooleanExpression condition, BlockStatement block) {
    super(condition, block);
  }

  public ForLoop(BooleanExpression condition, BlockStatement block, Statement initializer, Statement afterBlock) {
    super(condition, block);
    this.initializer = initializer;
    this.afterBlock = afterBlock;
  }

  @Override
  public String generate(Context context) {
    StringBuilder builder = new StringBuilder("for (");

    if (initializer != null) {
      builder.append(initializer.generate(context));
      if (initializer instanceof Variable) {
        context.addVariable((Variable) initializer);
      }
    }
    
    if (!builder.toString().endsWith(";"))
      builder.append(";");
    
    builder.append(" ").append(getCondition().generate(context)).append("; ");

    if (afterBlock != null) {
      String afterOutput = afterBlock.generate(context).trim();

      if (afterOutput.endsWith(";")) {
        afterOutput = afterOutput.substring(0, afterOutput.length() - 1);
      }

      builder.append(afterOutput);
    }

    builder.append(") {\n")
        .append(getBlock().generate(Context.create(context)))
        .append("\n}\n");

    return builder.toString();
  }
}
