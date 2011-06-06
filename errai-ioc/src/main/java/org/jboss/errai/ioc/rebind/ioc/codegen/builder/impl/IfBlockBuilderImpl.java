package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
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

    public BlockBuilder<AbstractStatementBuilder> else_() {
        return new BlockBuilder<AbstractStatementBuilder>(ifBlock.getElseBlock(), new BuildCallback<AbstractStatementBuilder>() {
            public AbstractStatementBuilder callback(Statement statement) {
                return IfBlockBuilderImpl.this;
            }
        });
    }
    
    private BlockBuilder<ElseBlockBuilder> _if_() {
        appendCallElement(new DeferredCallElement(new DeferredCallback() {
            public void doDeferred(CallWriter writer, Context context, Statement statement) {
                if (ifBlock.getCondition().getOperator() == null) {
                    statement = GenUtil.convert(context, statement, MetaClassFactory.get(Boolean.class));
                } else {
                    ifBlock.getCondition().getOperator().canBeAppliedLhs(statement.getType());
                }

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
}