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
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.IfBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallWriter;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;

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
    ifBlock = new IfBlock(new BooleanExpressionBuilder());
    return _if_();
  }

  public BlockBuilder<ElseBlockBuilder> if_(BooleanOperator op, Statement rhs) {
    ifBlock = new IfBlock(new BooleanExpressionBuilder(rhs, op));
    return _if_();
  }

  public BlockBuilder<ElseBlockBuilder> if_(BooleanOperator op, Object rhs) {
    Statement rhsStatement = GenUtil.generate(context, rhs);
    return if_(op, rhsStatement);
  }

  private BlockBuilder<ElseBlockBuilder> _if_() {
    appendCallElement(new DeferredCallElement(new DeferredCallback() {
      public void doDeferred(CallWriter writer, Context context, Statement statement) {
        statement = validateOrConvertLhs(statement);

        ifBlock.getCondition().setLhsExpr(writer.getCallString());
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
    // generate to internally set the type
    lhs.generate(context);

    lhs = validateOrConvertLhs(lhs);

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

  private Statement validateOrConvertLhs(Statement lhs) {
    if (ifBlock.getCondition().getOperator() == null) {
      lhs = GenUtil.convert(context, lhs, MetaClassFactory.get(Boolean.class));
    }
    else {
      ifBlock.getCondition().getOperator().assertCanBeApplied(lhs.getType());
    }

    return lhs;
  }
}