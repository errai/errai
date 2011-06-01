package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface IfBlockBuilder extends Statement {
    IfBlockBuilder if_(BooleanOperator op, Statement rhs);
    
    IfBlockBuilder if_(BooleanOperator op, Object rhs);

    IfBlockBuilder if_();
}
