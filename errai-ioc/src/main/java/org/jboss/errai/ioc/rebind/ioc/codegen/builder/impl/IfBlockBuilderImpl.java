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

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.IfBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallWriter;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.NullLiteral;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

/**
 * StatementBuilder to generate if blocks.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IfBlockBuilderImpl extends AbstractStatementBuilder implements IfBlockBuilder, ElseBlockBuilder {
  private IfBlock ifBlock;

  protected IfBlockBuilderImpl(Context context, CallElementBuilder callElementBuilder) {
    super(context, callElementBuilder);
  }

  protected IfBlockBuilderImpl(Context context, CallElementBuilder callElementBuilder, IfBlock ifBlock) {
    super(context, callElementBuilder);
    this.ifBlock = ifBlock;
  }

  public BlockBuilder<ElseBlockBuilder> if_() {
    return doIfLHSChained(new BooleanExpressionBuilder());
  }

  public BlockBuilder<ElseBlockBuilder> if_(Statement booleanExpr) {
    return doIfUnchained(booleanExpr);
  }

  public BlockBuilder<ElseBlockBuilder> if_(BooleanOperator op, Statement rhs) {
    if (rhs == null) rhs = NullLiteral.INSTANCE;
    return doIfLHSChained(new BooleanExpressionBuilder(rhs, op));
  }

  public BlockBuilder<ElseBlockBuilder> if_(BooleanOperator op, Object rhs) {
    Statement rhsStatement = GenUtil.generate(context, rhs);
    return if_(op, rhsStatement);
  }

  private BlockBuilder<ElseBlockBuilder> doIfUnchained(final Statement condition) {
    ifBlock = new IfBlock(condition);

    appendCallElement(new DeferredCallElement(new DeferredCallback() {
      public void doDeferred(CallWriter writer, Context context, Statement statement) {
        writer.reset();
        writer.append(ifBlock.generate(Context.create(context)));
      }
    }));

    return new BlockBuilder<ElseBlockBuilder>(ifBlock.getBlock(), new BuildCallback<ElseBlockBuilder>() {
      public ElseBlockBuilder callback(Statement statement) {
        return IfBlockBuilderImpl.this;
      }
    });
  }

  /**
   * The LHS value is on the current callstack. So we grab the value from there at generation time.
   *
   * @param condition
   * @return block builder
   */
  private BlockBuilder<ElseBlockBuilder> doIfLHSChained(final BooleanExpressionBuilder condition) {
    ifBlock = new IfBlock(condition);

    appendCallElement(new DeferredCallElement(new DeferredCallback() {
      public void doDeferred(CallWriter writer, Context context, Statement statement) {
        condition.setLhs(statement);
        condition.setLhsExpr(writer.getCallString());
        writer.reset();
        writer.append(ifBlock.generate(Context.create(context)));
      }
    }));

    return new BlockBuilder<ElseBlockBuilder>(ifBlock.getBlock(), new BuildCallback<ElseBlockBuilder>() {
      public ElseBlockBuilder callback(Statement statement) {
        return IfBlockBuilderImpl.this;
      }
    });
  }

  public BlockBuilder<AbstractStatementBuilder> else_() {
    return new BlockBuilder<AbstractStatementBuilder>(ifBlock.getElseBlock(), new BuildCallback<AbstractStatementBuilder>() {
      public AbstractStatementBuilder callback(Statement statement) {
        return IfBlockBuilderImpl.this;
      }
    });
  }

  public BlockBuilder<ElseBlockBuilder> elseif_(Statement lhs) {
    return elseif_(lhs, null, null);
  }

  public BlockBuilder<ElseBlockBuilder> elseif_(Statement lhs, BooleanOperator op, Statement rhs) {
    if (lhs.getType() == null)
      lhs.generate(context);

    IfBlock elseIfBlock = new IfBlock(new BooleanExpressionBuilder(lhs, rhs, op));
    ifBlock.setElseIfBlock(elseIfBlock);
    return _elseif_(elseIfBlock);
  }

  public BlockBuilder<ElseBlockBuilder> elseif_(Statement lhs, BooleanOperator op, Object rhs) {
    Statement rhsStatement = GenUtil.generate(context, rhs);
    return elseif_(lhs, op, rhsStatement);
  }

  private BlockBuilder<ElseBlockBuilder> _elseif_(final IfBlock elseIfBlock) {
    return new BlockBuilder<ElseBlockBuilder>(elseIfBlock.getBlock(), new BuildCallback<ElseBlockBuilder>() {
      public ElseBlockBuilder callback(Statement statement) {
        return new IfBlockBuilderImpl(context, callElementBuilder, elseIfBlock);
      }
    });
  }
}