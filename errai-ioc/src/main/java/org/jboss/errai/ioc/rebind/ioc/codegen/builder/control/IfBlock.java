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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BooleanExpressionBuilder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IfBlock extends AbstractBlockConditional {
  private BlockStatement elseBlock = new BlockStatement();
  private IfBlock elseIfBlock;

  public IfBlock(BooleanExpressionBuilder condition) {
    super(condition);
  }

  public IfBlock(BooleanExpressionBuilder condition, Statement block) {
    super(condition, new BlockStatement(block));
  }

  public BooleanExpressionBuilder getCondition() {
    return (BooleanExpressionBuilder) super.getCondition();
  }

  public BlockStatement getElseBlock() {
    return elseBlock;
  }

  public IfBlock getElseIfBlock() {
    return elseIfBlock;
  }

  public void setElseIfBlock(IfBlock elseIfBlock) {
    this.elseIfBlock = elseIfBlock;
  }

  public String generate(Context context) {
    StringBuilder builder = new StringBuilder("if ");
    builder.append("(").append(getCondition().generate(context)).append(") ");

    builder.append("{\n");

    if (getBlock() != null) {
      builder.append(getBlock().generate(context));
    }

    builder.append("\n} ");

    if (elseIfBlock != null) {
      builder.append("else ").append(elseIfBlock.generate(context));
      return builder.toString();
    }

    if (elseBlock != null && !elseBlock.isEmpty()) {
      builder.append("else { ").append(elseBlock.generate(context)).append("\n} ");
      return builder.toString();
    }

    return builder.toString();
  }
}
