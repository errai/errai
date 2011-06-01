package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface ElseBlockBuilder extends Statement {
    Statement else_(Statement block);
    ElseBlockBuilder elseIf_(BooleanOperator op, Statement rhs, Statement block);
    ElseBlockBuilder elseIf_(BooleanOperator op, Object rhs, Statement block);
}
