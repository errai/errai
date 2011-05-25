package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.HasScope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.TypeNotIterableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedVariableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.JavaReflectionClass;

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
    
    protected void assertVariableInScope(Variable var) {
        if (var == null || !scope.containsVariable(var))
            throw new UndefinedVariableException("Variable:" + var);
    }
    
    protected void assertVariableIsIterable(Variable var) {
        try {
            Class<?> cls = Class.forName(var.getType().getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            if (!cls.isArray() && !Iterable.class.isAssignableFrom(cls))
                throw new TypeNotIterableException("Variable:" + var);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    protected MetaClass getVariableComponentType(Variable var) {
        try {
            Class<?> cls = Class.forName(var.getType().getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());
            
            if(cls.getComponentType()!=null)
                return new JavaReflectionClass(cls.getComponentType());
            
            return null;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public Scope getScope() {
        return scope;
    }
}
