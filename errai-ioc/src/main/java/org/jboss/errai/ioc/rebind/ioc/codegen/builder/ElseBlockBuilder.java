package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Operator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.AbstractStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilder;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface ElseBlockBuilder extends Statement, Builder {
    /**
     * @param lhs
     * @param operator
     * @param rhs
     * @return
     */
    BlockBuilder<ElseBlockBuilder> elseif(Statement lhs, Operator operator, Statement rhs);

    /**
     * Compared RHS with the LHS of the initial IF block.
     *
     * @param operator
     * @param rhs
     * @return
     */
    BlockBuilder<ElseBlockBuilder> elseif(Operator operator, Statement rhs);

    BlockBuilder<AbstractStatementBuilder> else_();
}
