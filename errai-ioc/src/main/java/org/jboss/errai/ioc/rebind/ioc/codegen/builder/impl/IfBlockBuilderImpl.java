package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.IfBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;

/**
 * StatementBuilder to generate if blocks.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IfBlockBuilderImpl extends AbstractStatementBuilder implements IfBlockBuilder, ElseBlockBuilder {

    private IfBlockBuilderImpl(AbstractStatementBuilder parent) {
        super(Context.create(parent.getContext()));
    }

    public static IfBlockBuilderImpl create(AbstractStatementBuilder parent) {
        return new IfBlockBuilderImpl(parent);
    }

    public ElseBlockBuilder if_(Statement block) {
        //   statement = new IfBlock(parent.statement, block);
        return this;
    }

    public IfBlock if_(Statement block, IfBlock elseIf) {
        //    statement = new IfBlock(parent.statement, block, elseIf);
        //    return (IfBlock) statement;
        return null;
    }

    public ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block) {
        //   statement = new IfBlock(new BooleanExpressionBuilder(parent.statement, rhs, op), block);
        return this;
    }

    public ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block) {
        Statement rhsStatement = GenUtil.generate(context, rhs);
        return if_(op, rhsStatement, block);
    }

    public IfBlock if_(BooleanOperator op, Statement rhs, Statement block, IfBlock elseIf) {
        //      statement = new IfBlock(new BooleanExpressionBuilder(parent.statement, rhs, op), block, elseIf);
        //       return (IfBlock) statement;
        return null;
    }

    public IfBlock if_(BooleanOperator op, Object rhs, Statement block, IfBlock elseIf) {
        Statement rhsStatement = GenUtil.generate(context, rhs);
        return if_(op, rhsStatement, block, elseIf);
    }

    public IfBlock else_(Statement block) {
        //    ((IfBlock) statement).setElseBlock(new BlockStatement(block));
        //    return (IfBlock) statement;
        return null;
    }
}