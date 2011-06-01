package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;

/**
 * StatementBuilder to generate loops.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IfBlockBuilderImpl extends AbstractStatementBuilder implements IfBlockBuilder, ElseBlockBuilder {

    private IfBlockBuilderImpl(AbstractStatementBuilder parent) {
        super(Context.create(parent.getContext()));
        this.parent = parent;
    }

    public static IfBlockBuilderImpl create(AbstractStatementBuilder parent) {
        return new IfBlockBuilderImpl(parent);
    }

    public ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block) {
        statement = new IfBlock(new BooleanExpressionBuilder(parent.statement, rhs, op), block);
        return this;
    }

    public ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block) {
        Statement rhsStatement = GenUtil.generate(context, rhs);
        statement = new IfBlock(new BooleanExpressionBuilder(parent.statement, rhsStatement, op), block);
        return this;
    }

    public ElseBlockBuilder if_(Statement block) {
        statement = new IfBlock(parent.statement, block);
        return this;
    }

    public Statement else_(Statement block) {
        ((IfBlock)statement).setElseBlock(block);
        return statement;
    }

    public ElseBlockBuilder elseIf_(BooleanOperator op, Statement rhs, Statement block) {
        ((IfBlock)statement).setElseIf(new IfBlock(new BooleanExpressionBuilder(parent.statement, rhs, op), block));
        return this;
    }
    
    public ElseBlockBuilder elseIf_(BooleanOperator op, Object rhs, Statement block) {
        Statement rhsStatement = GenUtil.generate(context, rhs);
        ((IfBlock)statement).setElseIf(new IfBlock(new BooleanExpressionBuilder(parent.statement, rhsStatement, op), block));
        return this;
    }
}
