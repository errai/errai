package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.HasScope;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBuilder.LoopBodyBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ScopedStatementBuilder extends AbstractStatementBuilder {

    private ScopedStatementBuilder(Scope scope) {
        super(scope);
    }

    public static ScopedStatementBuilder createInScopeOf(HasScope parent) {
        return new ScopedStatementBuilder(parent.getScope());
    }

    public LoopBodyBuilder foreach(String loopVarName) {
        return LoopBuilder.createInScopeOf(this).foreach(loopVarName);
    }

    public LoopBodyBuilder foreach(String loopVarName, Class<?> loopVarType) {
        return foreach(loopVarName, MetaClassFactory.get(loopVarType));
    }
    
    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType) {
        return LoopBuilder.createInScopeOf(this).foreach(loopVarName, loopVarType);
    }
    
    public LoopBodyBuilder foreach(String loopVarName, String collectionVarName) {
        return LoopBuilder.createInScopeOf(this).foreach(loopVarName, collectionVarName);
    }
    
    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType, String collectionVarName) {
        return LoopBuilder.createInScopeOf(this).foreach(loopVarName, loopVarType, collectionVarName);
    }
    
    public LoopBodyBuilder foreach(String loopVarName, Class<?> loopVarType, String collectionVarName) {
        return foreach(loopVarName, MetaClassFactory.get(loopVarType), collectionVarName);
    }
    
    public InvocationBuilder invoke(String methodName, Variable... parameters) {
        return InvocationBuilder.createInScopeOf(this).invoke(methodName, parameters);
    }
}
