package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface IfBlockBuilder extends Statement, Builder {
    ElseBlockBuilder if_(Statement block);
    ElseBlockBuilder if_(Statement block, IfBlock elseIf);
    
    ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block);
    ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block, IfBlock elseIf);
    
    ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block);
    ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block, IfBlock elseIf);
}
