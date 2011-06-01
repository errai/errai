package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface IfBlockBuilder extends Statement {
    ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block);
    Statement if_(BooleanOperator op, Statement rhs, Statement block, IfBlock elseIf);

    ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block);
    Statement if_(BooleanOperator op, Object rhs, Statement block, IfBlock elseIf);
    
    ElseBlockBuilder if_(Statement block);
    IfBlock if_(Statement block, IfBlock elseIf);
}
