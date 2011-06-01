package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface IfBlockBuilder extends Statement {
    ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block);
    
    ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block);
    
    ElseBlockBuilder if_(Statement block);
}
