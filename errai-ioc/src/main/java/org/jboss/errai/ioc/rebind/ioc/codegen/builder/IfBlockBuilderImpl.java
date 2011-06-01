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
public class IfBlockBuilderImpl extends AbstractStatementBuilder implements IfBlockBuilder {

    private IfBlockBuilderImpl(AbstractStatementBuilder parent) {
        super(Context.create(parent.getContext()));
        this.parent = parent;
    }

    public static IfBlockBuilderImpl create(AbstractStatementBuilder parent) {
        return new IfBlockBuilderImpl(parent);
    }

    public IfBlockBuilderImpl if_(BooleanOperator op, Statement rhs) {
        statement = new IfBlock(new BooleanExpressionBuilder(parent.statement, rhs, op), null);
        return this;
    }

    public IfBlockBuilder if_(BooleanOperator op, Object rhs) {
        Statement rhsStatement = GenUtil.generate(context, rhs);
        statement = new IfBlock(new BooleanExpressionBuilder(parent.statement, rhsStatement, op), null);
        return this;
    }

    public IfBlockBuilderImpl if_() {
        return null;
    }
}
