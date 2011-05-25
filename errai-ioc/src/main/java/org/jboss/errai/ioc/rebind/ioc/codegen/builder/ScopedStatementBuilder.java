package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.HasScope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBuilder.LoopBodyBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * 
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
    
    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType) {
        return LoopBuilder.createInScopeOf(this).foreach(loopVarName, loopVarType);
    }
    
    public LoopBodyBuilder foreach(String loopVarName, String sequenceVarName) {
        return LoopBuilder.createInScopeOf(this).foreach(loopVarName, sequenceVarName);
    }
    
    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType, String sequenceVarName) {
        return LoopBuilder.createInScopeOf(this).foreach(loopVarName, loopVarType, sequenceVarName);
    }
}
