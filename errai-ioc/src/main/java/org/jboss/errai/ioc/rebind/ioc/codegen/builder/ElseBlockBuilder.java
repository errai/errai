package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.AbstractStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilder;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface ElseBlockBuilder extends Statement, Builder {
    BlockBuilder<AbstractStatementBuilder> else_();

    BlockBuilder<ElseBlockBuilder> elseif_(Statement lhs);

    BlockBuilder<ElseBlockBuilder> elseif_(Statement lhs, BooleanOperator op, Statement rhs);

    BlockBuilder<ElseBlockBuilder> elseif_(Statement lhs, BooleanOperator op, Object rhs);
}
