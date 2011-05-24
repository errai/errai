package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.HasScope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedVariableException;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractStatementBuilder implements HasScope {
    protected Scope scope = null;
    
    protected AbstractStatementBuilder(Scope scope) {
        this.scope = scope;
    }
    
    protected void assertVariableInScope(String varName) {
        if (varName == null || !scope.containsVariable(varName))
            throw new UndefinedVariableException("Variable:" + varName);
 }
    
    public Scope getScope() {
        return scope;
    }
}
