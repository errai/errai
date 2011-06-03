package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
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

    public ElseBlockBuilder if_(final Statement block) {
        ifBlock = new IfBlock(new BooleanExpressionBuilder(), block);
        return _if_();
    }
    
    public ElseBlockBuilder if_(Statement block, IfBlock elseIf) {
        ifBlock = new IfBlock(new BooleanExpressionBuilder(), block, elseIf);
        return _if_();
    }

    public ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block) {
        ifBlock = new IfBlock(new BooleanExpressionBuilder(rhs, op), block);
        return _if_();
    }

    public ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block, IfBlock elseIf) {
        ifBlock = new IfBlock(new BooleanExpressionBuilder(rhs, op), block, elseIf);
        return _if_();
    }
    
    public ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block) {
        Statement rhsStatement = GenUtil.generate(context, rhs);
        return if_(op, rhsStatement, block);
    }
    
    public ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block, IfBlock elseIf) {
        Statement rhsStatement = GenUtil.generate(context, rhs);
        return if_(op, rhsStatement, block, elseIf);
    }
    
    public ElseBlockBuilder else_(Statement elseBlock) {
        ifBlock.setElseBlock(elseBlock);
        return this;
    }

    private IfBlockBuilderImpl _if_() {
        appendCallElement(new DeferredCallElement(new DeferredCallback() {
            public void doDeferred(CallWriter writer, Context context, Statement statement) {
                ifBlock.getCondition().setLhsExpr(writer.getCallString());
                writer.reset();
                writer.append(ifBlock.generate(Context.create(context)));
            }
        }));
        return this;
    }
}