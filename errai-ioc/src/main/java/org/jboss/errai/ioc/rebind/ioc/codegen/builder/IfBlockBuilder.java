package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.AbstractStatementBuilder;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface IfBlockBuilder extends Statement, Builder {
    ElseBlockBuilder if_(Statement block);
    AbstractStatementBuilder if_(Statement block, Statement elseIf);
    
    ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block);
    AbstractStatementBuilder if_(BooleanOperator op, Statement rhs, Statement block, Statement elseIf);
    
    ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block);
    AbstractStatementBuilder if_(BooleanOperator op, Object rhs, Statement block, Statement elseIf);
}
