package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilder;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface IfBlockBuilder extends Statement, Builder {
    BlockBuilder<ElseBlockBuilder> if_();

    BlockBuilder<ElseBlockBuilder> if_(BooleanOperator op, Statement rhs);

    BlockBuilder<ElseBlockBuilder> if_(BooleanOperator op, Object rhs);
}
